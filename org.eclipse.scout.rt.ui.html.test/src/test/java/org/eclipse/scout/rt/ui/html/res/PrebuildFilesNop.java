package org.eclipse.scout.rt.ui.html.res;

import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.Replace;

/**
 * This class is a NOP replacement for the original {@link PrebuildFiles} platform listener. Since pre-building of files
 * takes a long time we don't want to do that while executing unit tests.
 */
@Replace
public class PrebuildFilesNop extends PrebuildFiles {

  @Override
  public void stateChanged(PlatformEvent event) {
    // NOP
  }

}
