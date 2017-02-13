package com.gmail.collinsmith70.diablo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.CvarStateAdapter;
import com.gmail.collinsmith70.libgdx.CommandProcessor;
import com.gmail.collinsmith70.libgdx.Console;
import com.gmail.collinsmith70.libgdx.GdxCommandManager;
import com.gmail.collinsmith70.libgdx.GdxCvarManager;
import com.gmail.collinsmith70.libgdx.GdxKeyMapper;
import com.gmail.collinsmith70.libgdx.audio.MusicController;
import com.gmail.collinsmith70.libgdx.audio.MusicVolumeController;
import com.gmail.collinsmith70.libgdx.audio.VolumeControlledMusicLoader;
import com.gmail.collinsmith70.libgdx.key.MappedKey;
import com.gmail.collinsmith70.libgdx.util.PropagatingInputProcessor;

import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import static com.gmail.collinsmith70.diablo.Diablo.client;

@SuppressWarnings({ "ConstantConditions", "unused", "WeakerAccess", "SameParameterValue" })
public class Client extends ApplicationAdapter {

  private static final String TAG = "Client";

  public final RenderableConsole console;
  public final AssetManager assets;

  private GdxCommandManager commands;
  private GdxCvarManager cvars;
  private GdxKeyMapper keys;

  private MusicController music;

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
    this.width = width;
    this.height = height;

    FileHandleResolver fhResolver = new InternalFileHandleResolver();
    this.assets = new AssetManager(fhResolver);
    assets.setLoader(Music.class,
        new VolumeControlledMusicLoader(fhResolver, new MusicVolumeController()));

    boolean usesStdOut = true;
    OutputStream consoleOut;
    if (usesStdOut) {
      consoleOut = System.out;
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
  public GdxCommandManager commands() {
    return commands;
  }

  @Nullable
  public GdxCvarManager cvars() {
    return cvars;
  }

  @Nullable
  public GdxKeyMapper keys() {
    return keys;
  }

  @Nullable
  public MusicController music() {
    return music;
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

    Calendar calendar = Calendar.getInstance();
    DateFormat format = DateFormat.getDateTimeInstance();
    console.println(format.format(calendar.getTime()));

    this.commands = new GdxCommandManager();
    this.cvars = new GdxCvarManager();
    this.keys = new GdxKeyMapper();

    List<Throwable> throwables;
    throwables = Commands.addTo(commands);
    for (Throwable t : throwables) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    throwables = Cvars.addTo(cvars);
    for (Throwable t : throwables) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    throwables = Keys.addTo(keys);
    for (Throwable t : throwables) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    // TODO: Conditionally enable console on Android
    //noinspection ConstantIfStatement
    if (true) {
      Keys.Console.assign(MappedKey.SECONDARY, Input.Keys.MENU);
    }

    CommandProcessor processor = new CommandProcessor(commands) {
      @Override
      public void onUnprocessed(@NonNull Console console, @NonNull String buffer) {
        super.onUnprocessed(console, buffer);
        console.format(
            "Unrecognized command \"%s\". To see available commands, type \"%s\"%n",
            buffer,
            Commands.help.getAlias());
      }
    };
    console.addProcessor(processor);
    console.addSuggestionProvider(processor);

    this.music = new MusicController(assets);
    music.play("audio/music/intro.ogg");

    setupCvars();

    Gdx.input.setCatchBackKey(true);
    Gdx.input.setCatchMenuKey(true);
    Gdx.input.setInputProcessor(newInputProcessor());

    this.batch = new SpriteBatch();
  }

  @Override
  public void resize(int width, int height) {
    this.width = width;
    this.height = height;
    console.resize(width, height);
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
    float x, y;
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
    Collection<RuntimeException> exceptions;

    Gdx.app.debug(TAG, "Saving CVARS...");
    exceptions = cvars.saveAll();
    for (RuntimeException e : exceptions) {
      console.println(e.getMessage());
    }

    Gdx.app.debug(TAG, "Saving key assignments...");
    exceptions = keys.saveAll();
    for (RuntimeException e : exceptions) {
      console.println(e.getMessage());
    }

    Gdx.app.debug(TAG, "Disposing client...");
    console.dispose();

    Gdx.app.debug(TAG, "Disposing assets...");
    assets.dispose();

    try {
      Gdx.app.debug(TAG, "Resetting stdout...");
      System.setOut(System.out);
      Gdx.app.debug(TAG, "Resetting stderr...");
      System.setErr(System.err);
    } catch (SecurityException ignored) {
    } finally {
      Gdx.app.debug(TAG, "Flushing console...");
      console.flush();
      console.close();
    }
  }

  private void setupCvars() {

    Cvars.Client.Display.ShowFPS.addStateListener(new CvarStateAdapter<Byte>() {
      @Override
      public void onChanged(@NonNull Cvar<Byte> cvar, @Nullable Byte from, @Nullable Byte to) {
        drawFpsMethod = to == null ? 0 : to;
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

  private class InputProcessor extends PropagatingInputProcessor {

    public InputProcessor() {
      super();
    }

    public InputProcessor(@NonNull com.badlogic.gdx.InputProcessor inputProcessor) {
      super(inputProcessor);
    }

    @Override
    public boolean keyDown(int keycode) {
      if (Keys.Console.isAssigned(keycode)) {
        console.setVisible(!console.isVisible());
        return true;
      } else if (console.isVisible()) {
        return console.keyDown(keycode);
      }

      return super.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
      if (console.isVisible()) {
        return console.keyUp(keycode);
      }

      return super.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char ch) {
      if (console.isVisible()) {
        return console.keyTyped(ch);
      }

      return super.keyTyped(ch);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
      if (console.isVisible()) {
        return console.touchDown(screenX, screenY, pointer, button);
      }

      return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
      if (console.isVisible()) {
        return console.touchUp(screenX, screenY, pointer, button);
      }

      return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
      if (console.isVisible()) {
        return console.touchDragged(screenX, screenY, pointer);
      }

      return super.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
      if (console.isVisible()) {
        return console.mouseMoved(screenX, screenY);
      }

      return super.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(int amount) {
      if (console.isVisible()) {
        return console.scrolled(amount);
      }

      return super.scrolled(amount);
    }
  }
}
