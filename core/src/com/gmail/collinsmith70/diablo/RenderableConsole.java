package com.gmail.collinsmith70.diablo;

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
import com.gmail.collinsmith70.cvar.SimpleCvarStateAdapter;
import com.gmail.collinsmith70.libgdx.Console;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class RenderableConsole extends Console implements Disposable {

  private static final String TAG = "RenderableConsole";

  private static final String BUFFER_PREFIX = ">";

  private final Client client;

  private final List<String> OUTPUT = new ArrayList<>();
  private float outputOffset;

  private boolean visible;

  @Nullable
  private BitmapFont font;

  private Texture modalBackgroundTexture;
  private Texture highlightBackgroundTexture;
  private Texture cursorTexture;

  private static final float CARET_BLINK_DELAY = 0.5f;
  private static final float CARET_HOLD_DELAY = 1.0f;
  private Timer.Task caretBlinkTask;
  private boolean showCaret;

  public RenderableConsole(@NonNull Client client, @NonNull OutputStream out) {
    super(out);
    this.client = client;
    this.font = null;
    this.visible = false;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean b) {
    if (this.visible != b) {
      this.visible = b;
      updateCaret();
      Gdx.input.setOnscreenKeyboardVisible(true);
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
    highlightBackgroundTexture = new Texture(solidColorPixmap);
    solidColorPixmap.dispose();

    Cvars.Client.Console.Font.addStateListener(new SimpleCvarStateAdapter<String>() {
      @Override
      public void onChanged(@NonNull Cvar<String> cvar, @Nullable String from,
                            @Nullable String to) {
        client.assets.load(to, BitmapFont.class);
        client.assets.finishLoadingAsset(to);
        font = client.assets.get(to);
      }
    });

    caretBlinkTask = new Timer.Task() {
      @Override
      public void run() {
        showCaret = !showCaret;
      }
    };

    clearBuffer();
    updateCaret();
  }

  private void updateCaret() {
    caretBlinkTask.cancel();
    Timer.schedule(caretBlinkTask, CARET_HOLD_DELAY, CARET_BLINK_DELAY);
    this.showCaret = true;
  }

  public void render(Batch b) {
    if (!visible) {
      return;
    }

    b.draw(modalBackgroundTexture, 0.0f, 0.0f, client.width(), client.height());
    if (font == null) {
      return;
    }

    final int height = client.height();
    String bufferContents = getBufferContents();
    GlyphLayout glyphs = font.draw(b, BUFFER_PREFIX + " " + bufferContents, 0, height);
    if (showCaret) {
      glyphs.setText(font, BUFFER_PREFIX + " " + bufferContents.substring(0, getCaretPosition()));
      b.draw(cursorTexture,
          glyphs.width, height - font.getCapHeight(),
          2, font.getCapHeight());
    }

    float position;
    int outputSize = OUTPUT.size();
    final float lineHeight = font.getLineHeight();
    if (lineHeight * outputSize >= height) {
      position = lineHeight;
      position += outputOffset;
    } else {
      position = height - lineHeight * outputSize;
    }

    for (ListIterator<String> it = OUTPUT.listIterator(outputSize); it.hasPrevious();) {
      String line = it.previous();
      if (position >= height) {
        break;
      }

      font.draw(b, line, 0.0f, position);
      position += lineHeight;
    }
  }

  @Override
  public void dispose() {
    cursorTexture.dispose();
    modalBackgroundTexture.dispose();
    highlightBackgroundTexture.dispose();
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
        return true;
      case Input.Keys.DOWN:
        return true;
      case Input.Keys.TAB:
        return true;
      default:
        return super.keyDown(keycode);
    }
  }

  @Override
  public boolean keyTyped(char ch) {
    if (Keys.Console.isAssigned(Input.Keys.valueOf(Character.toString(ch)))) {
      return true;
    }

    boolean keyTyped = super.keyTyped(ch);
    updateCaret();
    return keyTyped;
  }

  @Override
  public boolean scrolled(int amount) {
    if (font == null) {
      return super.scrolled(amount);
    }

    switch (amount) {
      case -1:
        outputOffset = Math.max(
            outputOffset - font.getLineHeight(),
            client.height() - (OUTPUT.size() * font.getLineHeight()));
        break;
      case 1:
        outputOffset = Math.min(
            outputOffset + font.getLineHeight(),
            client.height() - 2*font.getLineHeight());
        break;
      default:
        Gdx.app.error(TAG, "Unexpected scroll amount: " + amount);
    }

    return super.scrolled(amount);
  }

  @Override
  public void println(@NonNull String str) {
    super.println(str);
    OUTPUT.add(str);
  }
}
