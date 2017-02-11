package com.gmail.collinsmith70.libgdx;

import com.google.common.base.Preconditions;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.gmail.collinsmith70.util.StringUtils;

import org.apache.commons.lang3.Validate;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@SuppressWarnings({ "WeakerAccess", "ConstantConditions", "unused", "UnusedReturnValue" })
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
  private final Set<SuggestionProvider> SUGGESTION_PROVIDERS;

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
  public Console(@NonNull OutputStream out) {
    super(out, true);
    this.STREAM_LISTENERS = new CopyOnWriteArraySet<>();
    this.SUGGESTION_PROVIDERS = new CopyOnWriteArraySet<>();
    this.COMMIT_PROCESSORS = new CopyOnWriteArraySet<>();
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
  public void println(@NonNull String str) {
    super.println(str);
    for (PrintStreamListener l : STREAM_LISTENERS) {
      l.onPrintln(str);
    }
  }

  /**
   * Helper method to prevent as many annoying implementations of
   * {@link String#format(String, Object...)}.
   *
   * @param format The format of the message
   * @param args   The arguments to place into the {@code message}
   *
   * @see #println(String)
   * @see String#format(String, Object...)
   */
  public void println(@NonNull String format, @Nullable Object... args) {
    println(String.format(format, args));
  }

  /**
   * Empties the buffer contents.
   */
  public void clearBuffer() {
    BUFFER.setLength(0);
    caret = 0;
  }

  /**
   * Clears and sets the buffer to the specified {@code charSequence}.
   *
   * @param charSequence The {@code CharSequence} to write
   */
  public void setBuffer(@NonNull CharSequence charSequence) {
    Preconditions.checkArgument(charSequence != null, "charSequence cannot be null");
    BUFFER.setLength(0);
    BUFFER.append(charSequence);
    caret = BUFFER.length();
    bufferModified();
  }

  /**
   * Appends the specified {@code charSequence} to the end of the buffer and sets the caret position
   * to the end.
   *
   * @param charSequence The {@code CharSequence} to write
   */
  public void appendToBuffer(@NonNull CharSequence charSequence) {
    if (charSequence.length() > 0) {
      BUFFER.append(charSequence);
      caret = BUFFER.length();
      bufferModified();
    }
  }

  /**
   * Appends the specified char {@code ch} to the end of the buffer and sets the caret position
   * to the end.
   *
   * @param ch The {@code char} to write
   */
  public void appendToBuffer(char ch) {
    BUFFER.append(ch);
    caret = BUFFER.length();
    bufferModified();
  }

  /**
   * Commits the current buffer contents to the underlying PrintStream and clears the buffer.
   *
   * @return The contents of the buffer
   *
   * @see #getBufferContents
   */
  @NonNull
  public final String commitBuffer() {
    String bufferContents = BUFFER.toString();
    println(bufferContents);
    clearBuffer();
    onCommit(bufferContents);

    boolean handled = false;
    for (Processor l : COMMIT_PROCESSORS) {
      handled = l.process(this, bufferContents);
      if (handled) {
        break;
      }
    }

    if (!handled) {
      for (Processor l : COMMIT_PROCESSORS) {
        l.onUnprocessed(this, bufferContents);
      }
    }

    return bufferContents;
  }

  /**
   * Called when the buffer is {@linkplain #commitBuffer committed}. Subclasses should override this
   * method instead of implementing and adding themselves as listeners.
   *
   * @param buffer The contents of the buffer
   */
  protected void onCommit(@NonNull String buffer) {}

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
   * Returns whether or not the buffer is empty.
   *
   * @return {@code true} if the buffer is empty, otherwise {@code false}
   */
  public boolean isBufferEmpty() {
    return BUFFER.length() == 0;
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
   * Called when the buffer is modified. Propagates event to all SuggestionProvider instances.
   */
  private void bufferModified() {
    String bufferContents = BUFFER.toString();
    onModified(bufferContents, caret);
    caretMoved();
  }

  /**
   * Called when the buffer is {@linkplain #bufferModified modified}. Subclasses should override
   * this method instead of implementing and adding themselves as listeners.
   *
   * @param buffer The contents of the buffer
   */
  protected void onModified(@NonNull String buffer, int position) {}

  /**
   * Called when the caret is moved. Propagates even to all SuggestionProvider instances.
   */
  private void caretMoved() {
    onCaretMoved(caret);
  }

  /**
   * Called when the caret is {@linkplain #caretMoved moved}. Subclasses should override this
   * method instead of implementing and adding themselves as listeners.
   *
   * @param position Position of the caret
   */
  protected void onCaretMoved(int position) {}

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
      case '\t':
        return true;
      default:
        BUFFER.insert(caret++, ch);
        bufferModified();
        return true;
    }
  }

  /**
   * Adds a SuggestionProvider to receive buffer events.
   *
   * @param l SuggestionProvider to add
   *
   * @return {@code true} if the specified SuggestionProvider was added, otherwise {@code false}
   */
  public boolean addSuggestionProvider(@NonNull SuggestionProvider l) {
    Validate.isTrue(l != null);
    return SUGGESTION_PROVIDERS.add(l);
  }

  /**
   * Removes the specified SuggestionProvider.
   *
   * @param l SuggestionProvider to remove
   *
   * @return {@code true} if the specified SuggestionProvider was removed, otherwise {@code false}
   */
  public boolean removeSuggestionProvider(@Nullable SuggestionProvider l) {
    return SUGGESTION_PROVIDERS.remove(l);
  }

  /**
   * Checks whether or not a specified SuggestionProvider will receive buffer events.
   *
   * @param l SuggestionProvider to check
   *
   * @return {@code true} if the specified SuggestionProvider will receive buffer events,
   *         otherwise {@code false}
   */
  public boolean containsSuggestionProvider(@Nullable SuggestionProvider l) {
    return SUGGESTION_PROVIDERS.contains(l);
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
      case Input.Keys.HOME:
        caret = 0;
        caretMoved();
        return true;
      case Input.Keys.END:
        caret = BUFFER.length();
        caretMoved();
        return true;
      case Input.Keys.TAB:
        if (caret != BUFFER.length()) {
          break;
        }

        boolean handled;
        String[] args = StringUtils.parseArgs(BUFFER);
        CharSequence bufferWrapper = new CharSequence() {
          @Override
          public int length() {
            return BUFFER.length();
          }

          @Override
          public char charAt(int index) {
            return BUFFER.charAt(index);
          }

          @Override
          public CharSequence subSequence(int start, int end) {
            return BUFFER.subSequence(start, end);
          }
        };
        for (SuggestionProvider l : SUGGESTION_PROVIDERS) {
          handled = l.suggest(this, bufferWrapper, args) > 0;
          if (handled) {
            break;
          }
        }

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

  public interface Processor {

    boolean process(@NonNull Console console, @NonNull String buffer);

    void onUnprocessed(@NonNull Console console, @NonNull String buffer);

  }

  public interface SuggestionProvider {

    @IntRange(from = 0)
    int suggest(@NonNull Console console, @NonNull CharSequence buffer, @NonNull String[] args);

  }

  public interface PrintStreamListener {

    void onPrintln(@NonNull CharSequence s);

  }
}
