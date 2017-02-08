package com.gmail.collinsmith70.libgdx;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

import org.apache.commons.lang3.Validate;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Console extends PrintStream implements InputProcessor {

  private static final String TAG = "Console";

  /**
   * Initial capacity of {@link #BUFFER}.
   */
  private static final int INITIAL_BUFFER_CAPACITY = 128;

  /**
   * PrintStream propagators.
   */
  @NonNull
  private final Set<PrintStreamListener> STREAM_LISTENERS;

  /**
   * Listeners for buffer events (e.g., modification, committing, flushing).
   */
  @NonNull
  private final Set<BufferListener> BUFFER_LISTENERS;

  /**
   * Listeners for buffer commits.
   */
  @NonNull
  private final Set<Processor> COMMIT_PROCESSORS;

  /**
   * Buffer for the console input.
   */
  @NonNull
  private final StringBuffer BUFFER;

  /**
   * Current position of the caret.
   */
  private int caret;

  /**
   * Constructs a console which will proxy and output to the specified OutputStream.
   *
   * @param out OutputStream to proxy
   */
  public Console(@Nullable OutputStream out) {
    super(out, true);
    this.STREAM_LISTENERS = new CopyOnWriteArraySet<PrintStreamListener>();
    this.BUFFER_LISTENERS = new CopyOnWriteArraySet<BufferListener>();
    this.COMMIT_PROCESSORS = new CopyOnWriteArraySet<Processor>();
    this.BUFFER = new StringBuffer(INITIAL_BUFFER_CAPACITY);
  }

  /**
   * Propagates the specified string {@code str} to the underlying PrintStream of this console
   * instance and propagates {@code str} to the current PrintStreamListener instances attached
   * to this console.
   *
   * @param str The message to output
   */
  @Override
  public void println(String str) {
    super.println(str);
    for (PrintStreamListener l : STREAM_LISTENERS) {
      l.onPrintln(str);
    }
  }

  /**
   * Empties the buffer contents.
   */
  public void clearBuffer() {
    BUFFER.setLength(0);
    caret = 0;
  }

  /**
   * Commits the current buffer contents to the underlying PrintStream and clears the buffer.
   *
   * @return The contents of the buffer
   *
   * @see #getBufferContents
   */
  @NonNull
  public String commitBuffer() {
    String bufferContents = BUFFER.toString();
    println(bufferContents);
    clearBuffer();
    for (BufferListener l : BUFFER_LISTENERS) {
      l.onCommit(bufferContents);
    }

    for (Processor l : COMMIT_PROCESSORS) {
      boolean handled = l.process(this, bufferContents);
      if (handled) {
        break;
      }
    }

    return bufferContents;
  }

  /**
   * Returns the contents of the buffer.
   *
   * @return The contents of the buffer
   */
  @NonNull
  public String getBufferContents() {
    return BUFFER.toString();
  }

  /**
   * Returns the length of the buffer.
   *
   * @return The length of the buffer
   */
  public int getBufferLength() {
    return BUFFER.length();
  }

  /**
   * Returns the position of the caret (i.e., the position where modifications are being made).
   * {@code 0} represents the start, while {@link #getBufferLength()} represents the end.
   *
   * @return The position of the caret
   */
  public int getCaretPosition() {
    return caret;
  }

  /**
   * Called when the buffer is modified. Propagates event to all BufferListener instances.
   */
  private void bufferModified() {
    String bufferContents = BUFFER.toString();
    for (BufferListener l : BUFFER_LISTENERS) {
      l.onModified(bufferContents, caret);
    }
  }

  /**
   * Called when the caret is moved. Propagates even to all BufferListener instances.
   */
  private void caretMoved() {
    for (BufferListener l : BUFFER_LISTENERS) {
      l.onCaretMoved(caret);
    }
  }

  /**
   * Handles the buffer I/O events and manages the caret position.
   *
   * @param ch The typed character.
   *
   * @return {@inheritDoc}
   */
  @Override
  public boolean keyTyped(char ch) {
    switch (ch) {
      case '\0':
        return true;
      case '\b':
        if (caret > 0) {
          BUFFER.deleteCharAt(--caret);
          bufferModified();
        }

        return true;
      case '\r':
      case '\n':
        if (BUFFER.length() > 0) {
          commitBuffer();
        }

        return true;
      case 127: // DEL
        if (caret < BUFFER.length()) {
          BUFFER.deleteCharAt(caret);
          bufferModified();
        }

        return true;
      default:
        BUFFER.insert(caret++, ch);
        bufferModified();
        return true;
    }
  }

  /**
   * Adds a BufferListener to receive buffer events.
   *
   * @param l BufferListener to add
   *
   * @return {@code true} if the specified BufferListener was added, otherwise {@code false}
   */
  public boolean addBufferListener(@NonNull BufferListener l) {
    Validate.isTrue(l != null);
    return BUFFER_LISTENERS.add(l);
  }

  /**
   * Removes the specified BufferListener.
   *
   * @param l BufferListener to remove
   *
   * @return {@code true} if the specified BufferListener was removed, otherwise {@code false}
   */
  public boolean removeBufferListener(@Nullable BufferListener l) {
    return BUFFER_LISTENERS.remove(l);
  }

  /**
   * Checks whether or not a specified BufferListener will receive buffer events.
   *
   * @param l BufferListener to check
   *
   * @return {@code true} if the specified BufferListener will receive buffer events,
   *         otherwise {@code false}
   */
  public boolean containsBufferListener(@Nullable BufferListener l) {
    return BUFFER_LISTENERS.contains(l);
  }

  /**
   * Adds a Processor to receive buffer commits.
   *
   * @param l Processor to add
   *
   * @return {@code true} if the specified Processor was added, otherwise {@code false}
   */
  public boolean addProcessor(@NonNull Processor l) {
    Validate.isTrue(l != null);
    return COMMIT_PROCESSORS.add(l);
  }

  /**
   * Removes the specified Processor.
   *
   * @param l Processor to remove
   *
   * @return {@code true} if the specified Processor was removed, otherwise {@code false}
   */
  public boolean removeProcessor(@Nullable Processor l) {
    return COMMIT_PROCESSORS.remove(l);
  }

  /**
   * Checks whether or not a specified Processor will receive buffer commits.
   *
   * @param l Processor to check
   *
   * @return {@code true} if the specified Processor will receive buffer commits, otherwise
   *         {@code false}
   */
  public boolean containsProcessor(@Nullable Processor l) {
    return COMMIT_PROCESSORS.contains(l);
  }

  /**
   * Adds a PrintStreamListener to receive {@link PrintStream#println(String)} events from the
   * proxied PrintStream.
   *
   * @param l PrintStreamListener to add
   *
   * @return {@code true} if the specified PrintStreamListener was added, otherwise {@code false}
   */
  public boolean addPrintStreamListener(@NonNull PrintStreamListener l) {
    Validate.isTrue(l != null);
    return STREAM_LISTENERS.add(l);
  }

  /**
   * Removes the specified PrintStreamListener.
   *
   * @param l PrintStreamListener to remove
   *
   * @return {@code true} if the specified PrintStreamListener was removed, otherwise {@code false}
   */
  public boolean removePrintStreamListener(@Nullable PrintStreamListener l) {
    return STREAM_LISTENERS.remove(l);
  }

  /**
   * Checks whether or not a specified PrintStreamListener will receive buffer events.
   *
   * @param l PrintStreamListener to check
   *
   * @return {@code true} if the specified PrintStreamListener will receive buffer events, otherwise
   *         {@code false}
   */
  public boolean containsPrintStreamListener(@Nullable PrintStreamListener l) {
    return STREAM_LISTENERS.contains(l);
  }

  @Override
  public boolean keyDown(int keycode) {
    switch (keycode) {
      case Input.Keys.LEFT:
        caret = Math.max(caret - 1, 0);
        caretMoved();
        return true;
      case Input.Keys.RIGHT:
        caret = Math.min(caret + 1, BUFFER.length());
        caretMoved();
        return true;
    }

    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return false;
  }

  @Override
  public boolean scrolled(int amount) {
    return false;
  }

  public interface BufferListener {

    void onModified(@NonNull String buffer, int position);

    void onCommit(@NonNull String buffer);

    void onCaretMoved(int position);

  }

  public interface Processor {

    boolean process(@NonNull Console console, @NonNull String buffer);

  }

  public interface PrintStreamListener {

    void onPrintln(@NonNull CharSequence s);

  }
}