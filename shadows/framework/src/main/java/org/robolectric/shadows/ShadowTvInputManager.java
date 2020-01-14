package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.media.tv.ITvInputManager;
import android.media.tv.TvContentRatingSystemInfo;
import android.media.tv.TvInputManager;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** A shadow implementation of {@link android.media.tv.TvInputManager}. */
@Implements(value = TvInputManager.class, minSdk = LOLLIPOP)
public class ShadowTvInputManager {

  @RealObject protected TvInputManager tvInputManager;

  @Implementation
  public List<TvContentRatingSystemInfo> getTvContentRatingSystemList() {
    return new ArrayList<>();
  }

  @Implementation
  protected void __constructor__(ITvInputManager iTvInputManager, int userId) {}
}
