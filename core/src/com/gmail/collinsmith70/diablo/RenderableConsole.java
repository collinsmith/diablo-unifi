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
import com.gmail.collinsmith70.command.Command;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.SimpleCvarStateAdapter;
import com.gmail.collinsmith70.libgdx.Console;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class RenderableConsole extends Console implements Console.BufferListener, Disposable {

  private static final String TAG = "RenderableConsole";

  private final Client client;

  private final List<String> OUTPUT = new ArrayList<String>();
  private float outputOffset;

  private boolean visible;
  private boolean showCaret = true;
  private String bufferPrefix = ">";

  @Nullable
  private BitmapFont font;

  private Texture modalBackgroundTexture;
  private Texture highlightBackgroundTexture;

  public RenderableConsole(@NonNull Client client, @Nullable OutputStream out) {
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
      Gdx.input.setOnscreenKeyboardVisible(true);
    }
  }

  @Nullable
  public BitmapFont getFont() {
    return font;
  }

  public void create() {
    Pixmap solidColorPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    solidColorPixmap.setColor(0.0f, 0.0f, 0.0f, 0.5f);
    solidColorPixmap.fill();
    modalBackgroundTexture = new Texture(solidColorPixmap);
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
    GlyphLayout glyphs = font.draw(b, bufferPrefix + " " + bufferContents, 0, height);
    if (showCaret) {
      glyphs.setText(font, bufferPrefix + " " + bufferContents.substring(0, getCaretPosition()));
      font.draw(b, "_", glyphs.width - 4, height - 1);
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

    return super.keyTyped(ch);
  }

  @Override
  public boolean scrolled(int amount) {
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
  public void println(String str) {
    super.println(str);
    OUTPUT.add(str);
  }

  @Override
  public void onCommit(@NonNull String buffer) {
    if (!buffer.isEmpty()) {
      boolean handled = false;
      String[] args = buffer.split("\\s+");
      Command command = client.commands.get(args[0]);
      if (command != null) {
        command.newInstance(args).execute();
      } else if (!handled) {
        println(String.format("Unrecognized command: \"%s\"", command));
      }
    }
  }

  @Override
  public void onModified(@NonNull String buffer, int position) {}

  @Override
  public void onCaretMoved(int position) {}
}
