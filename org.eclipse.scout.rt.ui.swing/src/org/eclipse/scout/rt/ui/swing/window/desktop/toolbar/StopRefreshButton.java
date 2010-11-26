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
package org.eclipse.scout.rt.ui.swing.window.desktop.toolbar;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.eclipse.scout.rt.ui.swing.Activator;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.JNavigationWidget.ButtonName;

public class StopRefreshButton extends NavigationWidgetButton {

  enum IconName {
    NONE,
    STOP,
    REFRESH
  }

  /**
   * Animates the glow around the stop/refresh button, using a sine function to calculate the
   * time each frame is displayed. That's why the first and the last frames are shown slightly
   * longer than the frames in the middle.
   * 
   * @author awe
   */
  class P_GlowThread extends Thread {
    @Override
    public void run() {
      synchronized (m_glowThreadLock) {
        boolean interrupted = false;
        while (this == m_glowThread) {
          m_glowFrame += m_glowDir;
          if (m_glowFrame == 0 || m_glowFrame == m_glowFrames.length - 1) {
            m_glowDir *= -1;
          }
          if (interrupted && m_glowFrame == 0) {
            m_showGlow = false;
            m_glowThread = null;
            return;
          }
          try {
            Thread.sleep(MS_PER_FRAME + getSineDelayForCurrentFrame());
          }
          catch (InterruptedException e) {
            // Allow the "glow" to finish its cycle
            interrupted = true;
          }
          repaintParent();
        }
      }
    }

    private int getSineDelayForCurrentFrame() {
      float degreeForFrame = 90 / (m_glowFrames.length - 1) * m_glowFrame;
      int delayPerFrame = (int) (Math.sin(toRadian(degreeForFrame)) * MS_PER_FRAME * 2);
      // System.out.println("glowFrame=" + glowFrame + " degreeForFrame=" + degreeForFrame + " delayPerFrame=" + delayPerFrame);
      return delayPerFrame;
    }

    private float toRadian(float degree) {
      return (float) (degree * (Math.PI / 180));
    }
  }

  private P_GlowThread m_glowThread;

  private Object m_glowThreadLock = new Object();

  private static final int FPS = 20;

  private static final int MS_PER_FRAME = 1000 / FPS;

  private static final Point GLOW_POSITION = new Point(64, -5);

  private IconName m_iconName = IconName.NONE;

  private boolean m_loading = false;

  private volatile boolean m_showGlow = false;

  private Icon[] m_stopIcon;

  private Icon[] m_glowFrames;

  private int m_glowFrame = 0;

  private int m_glowDir = 1;

  public StopRefreshButton(JComponent parent) {
    super(ButtonName.STOP_REFRESH, parent);
  }

  void loadGlowAnimation(String iconUrl) {
    m_glowFrames = new Icon[10];
    for (int i = 0; i < 10; i++) {
      String frameUrl = iconUrl + "_" + i;
      m_glowFrames[i] = Activator.getIcon(frameUrl);
    }
  }

  public void showRefreshIcon() {
    setIconName(IconName.REFRESH);
  }

  public void showStopIcon() {
    setIconName(IconName.STOP);
  }

  public void showNoIcon() {
    setIconName(IconName.NONE);
  }

  private void setIconName(IconName iconName) {
    IconName oldIconName = this.m_iconName;
    this.m_iconName = iconName;
    if (oldIconName != this.m_iconName) {
      repaintParent();
    }
  }

  public boolean isLoading() {
    return m_loading;
  }

  public void setLoading(boolean loading) {
    this.m_loading = loading;
    if (loading) {
      startGlow();
      showStopIcon();
    }
    else {
      showRefreshIcon();
      killGlow();
    }
  }

  private void killGlow() {
    m_glowThread.interrupt();
  }

  private void startGlow() {
    m_showGlow = true;
    m_glowFrame = 0;
    m_glowDir = 1;
    m_glowThread = new P_GlowThread();
    m_glowThread.start();
  }

  void loadStopIcon(String iconUrl) {
    m_stopIcon = loadTriStateIcon(iconUrl);
  }

  @Override
  protected void paintButton(Component c, Graphics g) {
    paintGlow(c, g);
    super.paintButton(c, g);
  }

  @Override
  protected void paintIcon(Component c, Graphics g) {
    if (m_iconName != IconName.NONE) {
      super.paintIcon(c, g);
    }
  }

  @Override
  Icon getIcon() {
    switch (m_iconName) {
      case STOP:
        return getTriStateIcon(m_stopIcon, isEnabled());
      case REFRESH:
        return super.getIcon();
    }
    return null;
  }

  private void paintGlow(Component c, Graphics g) {
    if (m_showGlow) {
      paintPressedImage(m_glowFrames[m_glowFrame], GLOW_POSITION, isPressed(), c, g);
    }
  }

  public IconName getIconName() {
    return m_iconName;
  }

  @Override
  public void buttonClicked(int button) {
    if (IconName.REFRESH == m_iconName) {
      performAction(getPrimaryAction());
    }
    else {
      performAction(getSecondaryAction());
    }
  }
}
