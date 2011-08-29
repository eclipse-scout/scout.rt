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
package org.eclipse.scout.rt.ui.swing.splash;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.UIManager;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.rt.ui.swing.ext.JFrameEx;
import org.osgi.framework.Version;

public class SplashWindow extends JFrameEx implements ISplashWindow {
  private static final long serialVersionUID = 1L;
  private String m_title;
  private String m_statusText = "...";
  private String m_versionText;
  private float m_animationPhase = -1;

  public SplashWindow(Frame owner) {
    setUndecorated(true);
    getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    Version v = Version.emptyVersion;
    if (Platform.getProduct() != null) {
      m_title = Platform.getProduct().getName();
      v = Version.parseVersion("" + Platform.getProduct().getDefiningBundle().getHeaders().get("Bundle-Version"));
    }
    Icon icon = UIManager.getIcon("Window.icon");
    if (icon instanceof ImageIcon) {
      setIconImage(((ImageIcon) icon).getImage());
    }
    Object icons = UIManager.getDefaults().get("Window.icons");
    if (icons instanceof List<?>) {
      List<Image> iconList = new ArrayList<Image>();
      for (Object ico : ((List<?>) icons)) {
        if (ico instanceof Image) {
          iconList.add((Image) ico);
        }
      }
      setIconImages(iconList);
    }
    setTitle(m_title);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setFont(new Font("Dialog", Font.PLAIN, 8));
    m_versionText = v.getMajor() + "." + v.getMinor() + "." + v.getMicro();
    // initial layout
    JComponent pane = (JComponent) getContentPane();
    pane.setLayout(new BorderLayout());

    Icon imgIcon = UIManager.getIcon("Splash.icon");
    if (imgIcon == null) {
      imgIcon = new P_EmptyIcon();
    }
    if (imgIcon != null) {
      JLabel iconLabel = new JLabel(new P_SplashScreen(imgIcon));
      iconLabel.setVerticalAlignment(JLabel.TOP);
      pane.add(iconLabel, BorderLayout.CENTER);
    }
    Color c = UIManager.getColor("Splash.text");
    if (c != null) {
      setForeground(c);
    }
    pack();
    // put in center of screen
    Dimension paneSize = getSize();
    Dimension screenSize = getToolkit().getScreenSize();
    setLocation((screenSize.width - paneSize.width) / 2, (screenSize.height - paneSize.height) / 2);
  }

  @Override
  public void showSplash() {
    setVisible(true);
    // start repaint timer
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          sleep(3000);
        }
        catch (InterruptedException e) {
        }
        while (isVisible()) {
          try {
            sleep(25);
          }
          catch (InterruptedException e) {
          }
          m_animationPhase = (((System.currentTimeMillis() / 3) % 1000) / 1000f) * 6f;
          if (m_animationPhase >= 0f && m_animationPhase <= 1f) {
            repaint();
          }
        }
      }
    };
    t.setDaemon(true);
    t.start();
  }

  @Override
  public void disposeSplash() {
    dispose();
  }

  @Override
  public void setStatusText(String s) {
    if (s == null) {
      s = "";
    }
    m_statusText = s;
    repaint();
  }

  private class P_SplashScreen implements Icon {
    private Icon m_splashIcon;
    private int m_highlightSize = 200;
    private Point m_versionLocation;
    private Point m_statusTextLocation;

    public P_SplashScreen(Icon splashIcon) {
      m_splashIcon = splashIcon;
      m_versionLocation = (Point) UIManager.get("Splash.versionLocation");
      m_statusTextLocation = (Point) UIManager.get("Splash.statusTextLocation");
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      FontMetrics fm = g.getFontMetrics();
      int w = getWidth();
      int h = getHeight();
      m_splashIcon.paintIcon(c, g, x, y);

      // paint glossy effect as progress indicator
      Graphics2D g2d = (Graphics2D) g;
      Color c1 = new Color(0x66ffffff, true);
      Color c2 = new Color(0x00ffffff, true);
      int phase = (int) (m_animationPhase * (w + m_highlightSize * 2)) - m_highlightSize;
      g2d.setPaint(new GradientPaint(phase - m_highlightSize, 0, c2, phase, 0, c1, true));
      g2d.fillRect(phase - m_highlightSize, 0, m_highlightSize, h);
      g2d.setPaint(new GradientPaint(phase, 0, c1, phase + m_highlightSize, 0, c2, true));
      g2d.fillRect(phase, 0, m_highlightSize, h);
      g2d.setPaint(getForeground());

      // use locations provided by the L/F or calculate default location of status- and version text.
      Point vloc = m_versionLocation;
      if (vloc == null) {
        vloc = new Point();
        vloc.x = 12;
        vloc.y = h - fm.getHeight() - 10;
      }
      g.setColor(getForeground());
      g.drawString(m_versionText, vloc.x, vloc.y);
      if (m_statusText != null) {
        Point sloc = m_statusTextLocation;
        if (sloc == null) {
          sloc = new Point();
          int offset = 12 + fm.stringWidth(m_versionText) + 4;
          sloc.x = Math.max((w - fm.stringWidth(m_statusText)) / 2, offset);
          sloc.y = h - fm.getHeight() - 10;
        }
        g.drawString(m_statusText, sloc.x, sloc.y);
      }
    }

    @Override
    public int getIconHeight() {
      return m_splashIcon.getIconHeight();
    }

    @Override
    public int getIconWidth() {
      return m_splashIcon.getIconWidth();
    }
  }

  private class P_EmptyIcon implements Icon {

    public P_EmptyIcon() {
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2d = (Graphics2D) g;
      Color c1 = new Color(0xffcccccc, true);
      Color c2 = new Color(0x00cccccc, true);
      g2d.setPaint(new GradientPaint(0, 0, c1, 0, getIconHeight(), c2, true));
      g2d.fillRect(0, 0, getIconWidth(), getIconHeight());
    }

    @Override
    public int getIconWidth() {
      return 360;
    }

    @Override
    public int getIconHeight() {
      return 280;
    }

  }

}
