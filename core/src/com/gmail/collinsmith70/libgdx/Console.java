package com.gmail.collinsmith70.libgdx;

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

public class Console extends PrintStream implements InputProcessor {

  /**
   * Initial capacity of {@link #BUFFER}.
   */
  private static final int INITIAL_BUFFER_CAPACITY = 128;

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

  @NonNull
  public final BufferOp buffer;

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
    this.SUGGESTION_PROVIDERS = new CopyOnWriteArraySet<>();
    this.COMMIT_PROCESSORS = new CopyOnWriteArraySet<>();
    this.BUFFER = new StringBuffer(INITIAL_BUFFER_CAPACITY);
    this.buffer = new BufferOp();
  }

  /**
   * Called when the buffer is {@linkplain BufferOp#commit committed}. Subclasses should override
   * this method instead of implementing and adding themselves as listeners.
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
          buffer.commit();
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
   * @return {@code true} if the specified Processor will receive buffer commits, otherwise {@code
   * false}
   */
  public boolean containsProcessor(@Nullable Processor l) {
    return COMMIT_PROCESSORS.contains(l);
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
          handled = l.suggest(this, bufferWrapper, args, 0) > 0;
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

  public class BufferOp implements CharSequence {

    public void clear() {
      BUFFER.setLength(0);
      caret = 0;
    }

    public void set(@Nullable CharSequence s) {
      BUFFER.setLength(0);
      append(s);
    }

    @NonNull
    public final String commit() {
      String bufferContents = BUFFER.toString();
      println(bufferContents);
      clear();
      onCommit(bufferContents);

      boolean handled = false;
      for (Processor l : COMMIT_PROCESSORS) {
        handled = l.process(Console.this, bufferContents);
        if (handled) {
          break;
        }
      }

      if (!handled) {
        for (Processor l : COMMIT_PROCESSORS) {
          l.onUnprocessed(Console.this, bufferContents);
        }
      }

      return bufferContents;
    }

    @Override
    public char charAt(int index) {
      return BUFFER.charAt(index);
    }

    public void setCharAt(int index, char ch) {
      BUFFER.setCharAt(index, ch);
      caret = index + 1;
    }

    @Override
    public int length() {
      return BUFFER.length();
    }

    public boolean isEmpty() {
      return BUFFER.length() == 0;
    }

    public void getChars(int srcBegin, int srcEnd, @NonNull char[] dst, int dstBegin) {
      BUFFER.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public int indexOf(@Nullable String str) {
      return BUFFER.indexOf(str);
    }

    public int indexOf(@Nullable String str, int fromIndex) {
      return BUFFER.indexOf(str, fromIndex);
    }

    public int lastIndexOf(@NonNull String str) {
      return BUFFER.lastIndexOf(str);
    }

    public int lastIndexOf(@NonNull String str, int fromIndex) {
      return BUFFER.lastIndexOf(str, fromIndex);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
      return BUFFER.offsetByCodePoints(index, codePointOffset);
    }

    public void append(boolean b) {
      BUFFER.append(b);
      caret = BUFFER.length();
      bufferModified();
    }

    public void append(char c) {
      BUFFER.append(c);
      caret = BUFFER.length();
      bufferModified();
    }

    public void append(@NonNull char[] str) {
      BUFFER.append(str);
      caret = BUFFER.length();
      bufferModified();
    }

    public void append(@NonNull char[] str, int offset, int len) {
      BUFFER.append(str, offset, len);
      caret = BUFFER.length();
      bufferModified();
    }

    public void append(@Nullable CharSequence s) {
      BUFFER.append(s);
      caret = BUFFER.length();
      bufferModified();
    }

    public void append(@Nullable CharSequence s, int start) {
      if (s == null) {
        append(s);
      } else {
        append(s, start, s.length());
      }
    }

    public void append(@Nullable CharSequence s, int start, int end) {
      BUFFER.append(s, start, end);
      caret = BUFFER.length();
      bufferModified();
    }

    public void append(double d) {
      BUFFER.append(d);
      caret = BUFFER.length();
      bufferModified();
    }

    public void append(float f) {
      BUFFER.append(f);
      caret = BUFFER.length();
      bufferModified();
    }

    public void append(int i) {
      BUFFER.append(i);
      caret = BUFFER.length();
      bufferModified();
    }

    public void append(long lng) {
      BUFFER.append(lng);
      caret = BUFFER.length();
      bufferModified();
    }

    public void append(@Nullable Object obj) {
      BUFFER.append(obj);
      caret = BUFFER.length();
      bufferModified();
    }

    public void append(@Nullable String str) {
      BUFFER.append(str);
      caret = BUFFER.length();
      bufferModified();
    }

    public void appendCodePoint(int codePoint) {
      BUFFER.appendCodePoint(codePoint);
      caret = BUFFER.length();
      bufferModified();
    }

    public int codePointAt(int index) {
      return BUFFER.codePointAt(index);
    }

    public int codePointBefore(int index) {
      return BUFFER.codePointBefore(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
      return BUFFER.codePointCount(beginIndex, endIndex);
    }

    public void insert(int offset, boolean b) {
      final int length = BUFFER.length();
      BUFFER.insert(offset, b);
      caret = offset + (length - BUFFER.length());
      bufferModified();
    }

    public void insert(int offset, char c) {
      final int length = BUFFER.length();
      BUFFER.insert(offset, c);
      caret = offset + (length - BUFFER.length());
      bufferModified();
    }

    public void insert(int offset, @NonNull char[] str) {
      final int length = BUFFER.length();
      BUFFER.insert(offset, str);
      caret = offset + (length - BUFFER.length());
      bufferModified();
    }

    public void insert(int index, @NonNull char[] str, int offset, int len) {
      final int length = BUFFER.length();
      BUFFER.insert(offset, str, offset, len);
      caret = offset + (length - BUFFER.length());
      bufferModified();
    }

    public void insert(int dstOffset, @Nullable CharSequence s) {
      final int length = BUFFER.length();
      BUFFER.insert(dstOffset, s);
      caret = dstOffset + (length - BUFFER.length());
      bufferModified();
    }

    public void insert(int dstOffset, @Nullable CharSequence s, int start) {
      if (s == null) {
        insert(dstOffset, s);
      } else {
        insert(dstOffset, s, start, s.length());
      }
    }

    public void insert(int dstOffset, @Nullable CharSequence s, int start, int end) {
      final int length = BUFFER.length();
      BUFFER.insert(dstOffset, s, start, end);
      caret = dstOffset + (length - BUFFER.length());
      bufferModified();
    }

    public void insert(int offset, double d) {
      final int length = BUFFER.length();
      BUFFER.insert(offset, d);
      caret = offset + (length - BUFFER.length());
      bufferModified();
    }

    public void insert(int offset, float f) {
      final int length = BUFFER.length();
      BUFFER.insert(offset, f);
      caret = offset + (length - BUFFER.length());
      bufferModified();
    }

    public void insert(int offset, int i) {
      final int length = BUFFER.length();
      BUFFER.insert(offset, i);
      caret = offset + (length - BUFFER.length());
      bufferModified();
    }

    public void insert(int offset, long l) {
      final int length = BUFFER.length();
      BUFFER.insert(offset, l);
      caret = offset + (length - BUFFER.length());
      bufferModified();
    }

    public void insert(int offset, @Nullable Object obj) {
      final int length = BUFFER.length();
      BUFFER.insert(offset, obj);
      caret = offset + (length - BUFFER.length());
      bufferModified();
    }

    public void insert(int offset, @Nullable String str) {
      final int length = BUFFER.length();
      BUFFER.insert(offset, str);
      caret = offset + (length - BUFFER.length());
      bufferModified();
    }

    @NonNull
    @Override
    public CharSequence subSequence(int start, int end) {
      return BUFFER.subSequence(start, end);
    }

    @NonNull
    public String substring(int start) {
      return BUFFER.substring(start);
    }

    @NonNull
    public String substring(int start, int end) {
      return BUFFER.substring(start, end);
    }

    @NonNull
    @Override
    public String toString() {
      return BUFFER.toString();
    }

  }

  public interface Processor {

    boolean process(@NonNull Console console, @NonNull String buffer);

    void onUnprocessed(@NonNull Console console, @NonNull String buffer);

  }

  public interface SuggestionProvider {

    @IntRange(from = 0)
    int suggest(@NonNull Console console, @NonNull CharSequence buffer,
                @NonNull String[] args, @IntRange(from = 0) int arg);

  }
}
