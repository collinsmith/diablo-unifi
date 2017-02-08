package com.gmail.collinsmith70.diablo;

import com.google.common.base.Supplier;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gmail.collinsmith70.command.CommandManager;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.SimpleCvarStateAdapter;
import com.gmail.collinsmith70.libgdx.CommandProcessor;
import com.gmail.collinsmith70.libgdx.CvarProcessor;
import com.gmail.collinsmith70.libgdx.GdxCvarManager;
import com.gmail.collinsmith70.libgdx.key.GdxKeyManager;
import com.gmail.collinsmith70.libgdx.key.MappedKey;
import com.gmail.collinsmith70.libgdx.util.MutexedInputProcessor;

import java.io.OutputStream;
import java.io.PrintStream;

import static com.gmail.collinsmith70.diablo.Diablo.client;

public class Client extends ApplicationAdapter {

  private static final String CLIENT_LOG_TAG = "Client";

  public final RenderableConsole console;
  public final AssetManager assets;
  public final CommandManager commands;

  private GdxCvarManager cvars;
  private GdxKeyManager keys;

  private com.badlogic.gdx.InputProcessor inputProcessor;

  private boolean forceWindowed;
  private boolean forceDrawFps;

  private Batch batch;

  private int width;
  private int height;

  private byte drawFpsMethod;

  public Client() {
    this(1280, 720);
  }

  public Client(int width, int height) {
    FileHandleResolver fhResolver = new InternalFileHandleResolver();
    this.assets = new AssetManager(fhResolver);
    this.commands = new CommandManager();

    boolean usesStdOut = true;
    OutputStream consoleOut;
    if (usesStdOut) {
      consoleOut = new PrintStream(System.out);
    } else {
      FileHandle consoleFileHandle = Gdx.files.local("console.out");
      consoleOut = consoleFileHandle.write(false);
    }

    this.console = new RenderableConsole(this, consoleOut);
    try {
      System.setOut(console);
      System.setErr(console);
    } catch (SecurityException e) {
      console.println("stdout could not be redirected to console: " + e.getMessage());
    }

    client = this;
  }

  @Nullable
  public GdxCvarManager cvars() {
    return cvars;
  }

  @Nullable
  public GdxKeyManager keys() {
    return keys;
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  public boolean isWindowedForced() {
    return forceWindowed;
  }

  public void setWindowedForced(boolean b) {
    this.forceWindowed = b;
  }

  public boolean isDrawFpsForced() {
    return forceDrawFps;
  }

  public void setDrawFpsForced(boolean b) {
    this.forceDrawFps = b;
  }

  @Override
  public void create() {
    console.create();

    this.cvars = new GdxCvarManager();
    this.keys = new GdxKeyManager();

    Commands.addTo(commands);
    Cvars.addTo(cvars);
    Keys.addTo(keys);
    // TODO: Conditionally enable console on Android
    if (true) {
      Keys.Console.assign(MappedKey.SECONDARY, Input.Keys.MENU);
    }

    console.addProcessor(new CommandProcessor(commands));
    console.addProcessor(new CvarProcessor(cvars));

    setupCvars();

    Gdx.input.setCatchBackKey(true);
    Gdx.input.setCatchMenuKey(true);
    Gdx.input.setInputProcessor(inputProcessor = newInputProcessor());

    this.batch = new SpriteBatch();
  }

  @Override
  public void resize(int width, int height) {
    this.width = width;
    this.height = height;
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    Batch b = this.batch;
    b.begin(); {
      if (console.isVisible()) {
        console.render(b);
      } else if (drawFpsMethod > 0 || forceDrawFps) {
        drawFps(b);
      }
    } b.end();
  }

  private void drawFps(Batch b) {
    BitmapFont font = console.getFont();
    if (font == null) {
      return;
    }

    GlyphLayout fps = new GlyphLayout(font, Integer.toString(Gdx.graphics.getFramesPerSecond()));
    float x = 0;
    float y = 0;
    int drawFpsMethod = this.drawFpsMethod;
    if (forceDrawFps && drawFpsMethod == 0) {
      drawFpsMethod = 1;
    }

    switch (drawFpsMethod) {
      case 1:
        x = 0;
        y = height();
        break;
      case 2:
        x = width() - fps.width;
        y = height();
        break;
      case 3:
        x = 0;
        y = 0;
        break;
      case 4:
        x = width() - fps.width;
        y = 0;
        break;
      default:
        return;
    }

    font.draw(b, fps, x, y);
  }

  @Override
  public void dispose() {
    Gdx.app.debug(CLIENT_LOG_TAG, "Saving CVARS...");
    cvars.saveAll();

    Gdx.app.debug(CLIENT_LOG_TAG, "Saving keys assignments...");
    keys.saveAll();

    Gdx.app.debug(CLIENT_LOG_TAG, "Disposing client...");
    console.dispose();

    Gdx.app.debug(CLIENT_LOG_TAG, "Disposing assets...");
    assets.dispose();

    try {
      Gdx.app.debug(CLIENT_LOG_TAG, "Resetting stdout...");
      System.setOut(System.out);
      Gdx.app.debug(CLIENT_LOG_TAG, "Resetting stderr...");
      System.setErr(System.err);
    } catch (SecurityException e) {
    } finally {
      Gdx.app.debug(CLIENT_LOG_TAG, "Flushing console...");
      console.flush();
      console.close();
    }
  }

  private void setupCvars() {

    Cvars.Client.Display.ShowFPS.addStateListener(new SimpleCvarStateAdapter<Byte>() {
      @Override
      public void onChanged(@NonNull Cvar<Byte> cvar, @Nullable Byte from, @Nullable Byte to) {
        drawFpsMethod = to;
      }
    });
  }

  private com.badlogic.gdx.InputProcessor newInputProcessor() {
    return new InputProcessor();
  }

  private com.badlogic.gdx.InputProcessor newInputProcessor(
      @NonNull com.badlogic.gdx.InputProcessor inputProcessor) {
    return new InputProcessor(inputProcessor);
  }

  private class InputProcessor extends MutexedInputProcessor {

    public InputProcessor() {
      super(console, new Supplier<Boolean>() {
        @Override
        public Boolean get() {
          return console.isVisible();
        }
      });
    }

    public InputProcessor(@NonNull com.badlogic.gdx.InputProcessor inputProcessor) {
      super(console, inputProcessor, new Supplier<Boolean>() {
        @Override
        public Boolean get() {
          return console.isVisible();
        }
      });
    }

    @Override
    public boolean keyDown(int keycode) {
      if (Keys.Console.isAssigned(keycode)) {
        console.setVisible(!console.isVisible());
        return true;
      }

      return super.keyDown(keycode);
    }
  }
}
