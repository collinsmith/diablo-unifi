package com.gmail.collinsmith70.libgdx.audio;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.audio.Music;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.CvarStateAdapter;
import com.gmail.collinsmith70.diablo.Cvars;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;

public class MusicVolumeController implements VolumeController<Music> {

  private static final String TAG = "MusicVolumeController";

  private final Collection<WeakReference<Music>> MANAGED;

  private boolean soundEnabled;
  private boolean musicEnabled;
  private float musicVolume;

  public MusicVolumeController() {
    this.MANAGED = new ArrayList<>();

    Cvars.Client.Sound.Enabled.addStateListener(new CvarStateAdapter<Boolean>() {
      @Override
      public void onChanged(@NonNull Cvar<Boolean> cvar,
                            @Nullable Boolean from, @Nullable Boolean to) {
        soundEnabled = to;
        refreshVolume();
      }
    });

    Cvars.Client.Sound.Music.Enabled.addStateListener(new CvarStateAdapter<Boolean>() {
      @Override
      public void onChanged(@NonNull Cvar<Boolean> cvar, @Nullable Boolean from, @Nullable
          Boolean to) {
        musicEnabled = to;
        refreshVolume();
      }
    });

    Cvars.Client.Sound.Music.Volume.addStateListener(new CvarStateAdapter<Float>() {
      @Override
      public void onChanged(@NonNull Cvar<Float> cvar, @Nullable Float from, @Nullable Float to) {
        musicVolume = to;
        refreshVolume();
      }
    });
  }

  private void refreshVolume() {
    float volume = getVolume();
    for (WeakReference<Music> container : MANAGED) {
      final Music track = container.get();
      if (track == null) {
        MANAGED.remove(track);
        continue;
      }

      track.setVolume(volume);
    }
  }

  @Override
  public boolean isEnabled() {
    return musicEnabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (musicEnabled != enabled) {
      this.musicEnabled = enabled;
      refreshVolume();
    }
  }

  @Override
  public float getVolume() {
    if (!soundEnabled || !musicEnabled) {
      return 0.0f;
    }

    return musicVolume;
  }

  @Override
  public void setVolume(float volume) {
    if (musicVolume != volume) {
      this.musicVolume = volume;
      refreshVolume();
    }
  }

  @Override
  public void manage(@NonNull WeakReference<Music> sound) {
    MANAGED.add(sound);
  }

  @Override
  public boolean remove(@Nullable Object obj) {
    return MANAGED.remove(obj);
  }
}
