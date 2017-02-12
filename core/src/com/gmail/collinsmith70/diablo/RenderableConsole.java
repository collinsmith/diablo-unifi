package com.gmail.collinsmith70.diablo;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Timer;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.CvarStateAdapter;
import com.gmail.collinsmith70.libgdx.Console;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class RenderableConsole extends Console implements Disposable {

  private static final String TAG = "RenderableConsole";

  private static final String BUFFER_PREFIX = ">";

  private final Client client;

  private float height;

  private final List<String> OUTPUT = new ArrayList<>();
  private final ByteArrayOutputStream BUFFER = new ByteArrayOutputStream();
  private int scrollOffset;
  private int scrollOffsetMin;

  private boolean visible;

  @Nullable
  private BitmapFont font;

  private Texture modalBackgroundTexture;
  private Texture hintBackgroundTexture;
  private Texture cursorTexture;

  private static final float CARET_BLINK_DELAY = 0.5f;
  private static final float CARET_HOLD_DELAY = 1.0f;
  private Timer.Task caretBlinkTask;
  private boolean showCaret;

  private final List<String> HISTORY;
  private ListIterator<String> historyIterator;

  private int clientWidth, clientHeight;
  private float textHeight;
  private float lineHeight;
  private int consoleHeight;
  private float outputHeight;
  private float consoleY;
  private float bufferY;
  private float outputY;

  private void recalculateScrollOffsetMin() {
    if (font == null) {
      return;
    }

    this.clientWidth = client.width();
    this.clientHeight = client.height();
    this.lineHeight = font.getLineHeight();
    this.textHeight = font.getCapHeight();
    this.consoleHeight = (int) (clientHeight * this.height);
    this.consoleY = clientHeight - consoleHeight;
    this.bufferY = consoleY + textHeight;
    this.outputY = bufferY + lineHeight;
    this.outputHeight = consoleHeight - lineHeight - textHeight;
    this.scrollOffsetMin = (int) (outputHeight / lineHeight) + 1;
    this.scrollOffset = Math.max(scrollOffset, scrollOffsetMin);
  }

  public RenderableConsole(@NonNull Client client, @NonNull OutputStream out) {
    super(out);
    this.client = client;
    this.font = null;
    this.visible = false;

    this.HISTORY = new ArrayList<>(64);
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean b) {
    if (this.visible != b) {
      this.visible = b;
      updateCaret();
      Gdx.input.setOnscreenKeyboardVisible(b);
      scrollOffset = OUTPUT.size();
    }
  }

  @Nullable
  public BitmapFont getFont() {
    return font;
  }

  public void clear() {
    OUTPUT.clear();
  }

  public void create() {
    Pixmap solidColorPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    solidColorPixmap.setColor(0.0f, 0.0f, 0.0f, 0.5f);
    solidColorPixmap.fill();
    modalBackgroundTexture = new Texture(solidColorPixmap);
    solidColorPixmap.dispose();

    solidColorPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    solidColorPixmap.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    solidColorPixmap.fill();
    cursorTexture = new Texture(solidColorPixmap);
    solidColorPixmap.dispose();

    solidColorPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    solidColorPixmap.setColor(0.0f, 0.0f, 1.0f, 0.5f);
    solidColorPixmap.fill();
    hintBackgroundTexture = new Texture(solidColorPixmap);
    solidColorPixmap.dispose();

    final Cvar.StateListener<Float> colorChangeListener = new CvarStateAdapter<Float>() {
      @Override
      public void onChanged(@NonNull Cvar<Float> cvar, @Nullable Float from, @Nullable Float to) {
        Preconditions.checkState(font != null, "font should not be null");
        Preconditions.checkState(to != null, "to should not be null");
        if (cvar.equals(Cvars.Client.Console.Color.a)) {
          font.getColor().a = to;
        } else if (cvar.equals(Cvars.Client.Console.Color.r)) {
          font.getColor().r = to;
        } else if (cvar.equals(Cvars.Client.Console.Color.g)) {
          font.getColor().g = to;
        } else if (cvar.equals(Cvars.Client.Console.Color.b)) {
          font.getColor().b = to;
        }
      }
    };

    Cvars.Client.Console.Font.addStateListener(new CvarStateAdapter<String>() {
      @Override
      public void onChanged(@NonNull Cvar<String> cvar, @Nullable String from,
                            @Nullable String to) {
        client.assets.load(to, BitmapFont.class);
        client.assets.finishLoadingAsset(to);
        font = client.assets.get(to);
        Cvars.Client.Console.Color.r.addStateListener(colorChangeListener);
        Cvars.Client.Console.Color.g.addStateListener(colorChangeListener);
        Cvars.Client.Console.Color.b.addStateListener(colorChangeListener);
        Cvars.Client.Console.Color.a.addStateListener(colorChangeListener);
        recalculateScrollOffsetMin();
      }
    });

    Cvars.Client.Console.Height.addStateListener(new CvarStateAdapter<Float>() {
      @Override
      public void onChanged(@NonNull Cvar<Float> cvar, @Nullable Float from, @Nullable Float to) {
        Preconditions.checkState(to != null, "to should not be null");
        height = to;
        recalculateScrollOffsetMin();
      }
    });

    caretBlinkTask = new Timer.Task() {
      @Override
      public void run() {
        showCaret = !showCaret;
      }
    };

    buffer.clear();
    updateCaret();
  }

  @Override
  protected void onCaretMoved(int position) {
    updateCaret();
  }

  private void updateCaret() {
    caretBlinkTask.cancel();
    Timer.schedule(caretBlinkTask, CARET_HOLD_DELAY, CARET_BLINK_DELAY);
    this.showCaret = true;
  }

  @SuppressWarnings("UnusedParameters")
  public void resize(int width, int height) {
    recalculateScrollOffsetMin();
  }

  public void render(Batch b) {
    if (!visible || font == null) {
      return;
    }

    b.draw(modalBackgroundTexture, 0.0f, consoleY - 4, clientWidth, consoleHeight + 4);

    final int x = 2;
    String bufferContents = getBufferContents();
    GlyphLayout glyphs = font.draw(b, BUFFER_PREFIX + bufferContents, x, bufferY - 2);
    b.draw(cursorTexture, x, bufferY, clientWidth, 2);
    if (showCaret) {
      if (getCaretPosition() != getBufferLength()) {
        glyphs.setText(font, BUFFER_PREFIX + bufferContents.substring(0, getCaretPosition()));
      }

      b.draw(cursorTexture, x + glyphs.width, consoleY - 2, 2, textHeight);
    }

    final float outputOffset = scrollOffset * lineHeight;
    if (outputOffset < outputHeight) {
      // offsets output to always appear that it starts at top of console window
      scrollOffset = Math.max(scrollOffset, scrollOffsetMin);
    }

    float position = outputY;
    final int outputSize = OUTPUT.size();
    if (scrollOffset > outputSize) {
      scrollOffset = outputSize;
      position += ((scrollOffsetMin - scrollOffset) * lineHeight);
    }

    for (ListIterator<String> it = OUTPUT.listIterator(scrollOffset); it.hasPrevious();) {
      if (position > clientHeight) {
        break;
      }

      String line = it.previous();
      font.draw(b, line, x, position);
      position += lineHeight;
    }
  }

  @Override
  public void dispose() {
    cursorTexture.dispose();
    modalBackgroundTexture.dispose();
    hintBackgroundTexture.dispose();
  }

  @Override
  public void onCommit(@NonNull String buffer) {
    HISTORY.add(buffer);
    historyIterator = HISTORY.listIterator(HISTORY.size());
  }

  @Override
  public boolean keyDown(int keycode) {
    switch (keycode) {
      case Input.Keys.MENU:
      case Input.Keys.ESCAPE:
      case Input.Keys.BACK:
        setVisible(false);
        return true;
      case Input.Keys.UP:
        if (historyIterator != null && historyIterator.hasPrevious()) {
          buffer.set(historyIterator.previous());
        }

        return true;
      case Input.Keys.DOWN:
        if (historyIterator != null && historyIterator.hasNext()) {
          buffer.set(historyIterator.next());
        } else {
          buffer.clear();
        }

        return true;
      default:
        return super.keyDown(keycode);
    }
  }

  @Override
  @SuppressWarnings("SimplifiableIfStatement")
  public boolean keyTyped(char ch) {
    if (Keys.Console.isAssigned(Input.Keys.valueOf(Character.toString(ch)))) {
      return true;
    }

    return super.keyTyped(ch);
  }

  @Override
  public boolean scrolled(int amount) {
    if (font == null) {
      return super.scrolled(amount);
    }

    switch (amount) {
      case -1:
        scrollOffset = Math.max(scrollOffset - 1, 0);
        break;
      case 1:
        scrollOffset = Math.min(scrollOffset + 1, OUTPUT.size());
        break;
      default:
        Gdx.app.error(TAG, "Unexpected scroll amount: " + amount);
    }

    return super.scrolled(amount);
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    Gdx.input.setOnscreenKeyboardVisible(true);
    return super.touchDown(screenX, screenY, pointer, button);
  }

  @Override
  public void write(@NonNull byte[] buf) throws IOException {
    super.write(buf);
    BUFFER.write(buf);
    for (byte b : buf) {
      if (b == '\n') {
        flush();
      }
    }
  }

  @Override
  public void write(int b) {
    super.write(b);
    BUFFER.write(b);
    if (b == '\n') {
      flush();
    }
  }

  @Override
  public void write(@NonNull byte[] buf, int off, int len) {
    super.write(buf, off, len);
    BUFFER.write(buf, off, len);
    for (int i = off; i < off + len; i++) {
      if (buf[i] == '\n') {
        flush();
      }
    }
  }

  @Override
  public void flush() {
    super.flush();
    OUTPUT.add(BUFFER.toString(Charset.forName("US-ASCII")));
    BUFFER.reset();
    int size = OUTPUT.size();
    if (scrollOffset == size - 1) {
      scrollOffset = size;
    }
  }
}
