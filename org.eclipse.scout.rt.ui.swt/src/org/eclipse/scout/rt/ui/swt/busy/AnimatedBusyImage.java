/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.busy;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

/**
 * Animated busy image as defined in org.eclipse.ui.forms#$nl$/icons/progress/ani/$index$.png
 */
public class AnimatedBusyImage implements Runnable {
  private static final int IMAGE_COUNT = 8;
  private static final int MILLISECONDS_OF_DELAY = 100;

  private static final Image[] animatedImage;

  static {
    animatedImage = new Image[IMAGE_COUNT];
    for (int i = 0; i < animatedImage.length; i++) {
      animatedImage[i] = createImage("$nl$/icons/progress/ani/" + (i + 1) + ".png");
    }
  }

  private static Image createImage(String subPath) {
    Bundle bundle = Platform.getBundle("org.eclipse.ui.forms");
    URL url = FileLocator.find(bundle, new Path(subPath), null);
    if (url == null) {
      return null;
    }
    try {
      url = FileLocator.resolve(url);
      return ImageDescriptor.createFromURL(url).createImage();
    }
    catch (IOException e) {
      return null;
    }
  }

  private final Display m_display;
  private boolean m_busy;
  private int m_animationIndex;

  public AnimatedBusyImage(Display display) {
    m_display = display;
  }

  public synchronized void setBusy(boolean b) {
    if (m_busy == b) {
      return;
    }
    m_busy = b;
    if (m_busy) {
      m_animationIndex = 0;
      m_display.timerExec(MILLISECONDS_OF_DELAY, this);
    }
  }

  public boolean isBusy() {
    return m_busy;
  }

  /**
   * Override this method to handle the next image
   */
  protected void notifyImage(Image image) {
  }

  @Override
  public synchronized void run() {
    if (!m_busy) {
      return;
    }
    notifyImage(animatedImage[m_animationIndex]);
    //next
    m_animationIndex = (m_animationIndex + 1) % IMAGE_COUNT;
    m_display.timerExec(MILLISECONDS_OF_DELAY, this);
  }

}
