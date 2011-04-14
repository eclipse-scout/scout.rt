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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;
import org.osgi.framework.Version;

public class EmbeddedSplashWindow implements ISplashWindow {
  private static final long serialVersionUID = 1L;
  private RootPaneContainer m_owner;
  //
  private JLabel m_splash;
  private String m_title;
  private String m_statusText = "...";
  private String m_versionText;
  private float m_animationPhase = -1;

  public EmbeddedSplashWindow(RootPaneContainer owner) {
    m_owner = owner;
    Version v = Version.emptyVersion;
    if (Platform.getProduct() != null) {
      m_title = Platform.getProduct().getName();
      v = Version.parseVersion("" + Platform.getProduct().getDefiningBundle().getHeaders().get("Bundle-Version"));
    }
    m_versionText = v.getMajor() + "." + v.getMinor() + "." + v.getMicro();
  }

  @Override
  public void showSplash() {
    Icon imgIcon = UIManager.getIcon("Splash.icon");
    if (imgIcon == null) {
      imgIcon = new P_EmptyIcon();
    }
    m_splash = new JLabel(new P_SplashScreen(imgIcon));
    m_splash.setPreferredSize(new Dimension(imgIcon.getIconWidth(), imgIcon.getIconHeight()));
    m_splash.setVerticalAlignment(JLabel.TOP);
    Color c = UIManager.getColor("Splash.text");
    if (c != null) {
      m_splash.setForeground(c);
    }
    JComponent glass = (JComponent) m_owner.getGlassPane();
    glass.setLayout(new P_GlassOverlayLayout());
    // add
    m_owner.getContentPane().removeAll();
    m_owner.getContentPane().setLayout(new P_Layout());
    m_owner.getContentPane().add(BorderLayoutEx.CENTER, m_splash);
    ((Frame) m_owner).pack();

    // start repaint timer
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          sleep(3000);
        }
        catch (InterruptedException e) {
        }
        while (m_splash != null) {
          try {
            sleep(25);
          }
          catch (InterruptedException e) {
          }
          if (m_splash != null && m_splash.isShowing()) {
            m_animationPhase = (((System.currentTimeMillis() / 3) % 1000) / 1000f) * 6f;
            if (m_animationPhase >= 0f && m_animationPhase <= 1f) {
              if (m_splash != null) {
                m_splash.repaint();
              }
            }
          }
        }
      }
    };
    t.setDaemon(true);
    t.start();
  }

  @Override
  public void disposeSplash() {
    m_splash = null;
    m_owner.getContentPane().removeAll();
    m_owner.getContentPane().setLayout(new BorderLayoutEx());
    JComponent glass = (JComponent) m_owner.getGlassPane();
    glass.removeAll();
    glass.setLayout(new FlowLayout());
    glass.setVisible(false);
  }

  @Override
  public void setStatusText(String s) {
    if (m_splash != null) {
      if (s == null) s = "";
      m_statusText = s;
      m_splash.repaint();
    }
  }

  private class P_SplashScreen implements Icon {
    private Icon m_splashIcon;
    private int m_highlightSize = 200;

    public P_SplashScreen(Icon splashIcon) {
      m_splashIcon = splashIcon;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      FontMetrics fm = g.getFontMetrics();
      int w = c.getWidth();
      int h = c.getHeight();
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
      g2d.setPaint(c.getForeground());
      g.drawString(m_versionText, 12, h - fm.getHeight() - 10);
      int offset = 12 + fm.stringWidth(m_versionText) + 4;
      if (m_statusText != null) {
        g.drawString(m_statusText, Math.max((w - fm.stringWidth(m_statusText)) / 2, offset), h - fm.getHeight() - 10);
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

  private class P_Layout extends AbstractLayoutManager2 {
    @Override
    protected void validateLayout(Container parent) {
    }

    @Override
    protected Dimension getLayoutSize(Container parent, int sizeflag) {
      if (parent.getComponentCount() > 0) {
        return parent.getComponent(0).getPreferredSize();
      }
      return new Dimension();
    }

    @Override
    public void layoutContainer(Container parent) {
      if (parent.getComponentCount() > 0) {
        Component c = parent.getComponent(0);
        Dimension d = c.getPreferredSize();
        int dx = Math.max(0, parent.getWidth() - d.width) / 2;
        int dy = Math.max(0, parent.getHeight() - d.height) / 2;
        c.setBounds(dx, dy, d.width, d.height);
      }
    }
  }

  private class P_GlassOverlayLayout extends AbstractLayoutManager2 {
    @Override
    protected void validateLayout(Container parent) {
    }

    @Override
    protected Dimension getLayoutSize(Container parent, int sizeflag) {
      if (parent.getComponentCount() > 0) {
        return parent.getComponent(0).getPreferredSize();
      }
      return new Dimension();
    }

    @Override
    public void layoutContainer(Container parent) {
      if (parent.getComponentCount() > 0) {
        Component c = parent.getComponent(0);
        Dimension d = c.getPreferredSize();
        int dx = Math.max(0, parent.getWidth() - d.width) / 2 - 45;
        int dy = Math.max(0, parent.getHeight() - d.height) / 2 + 70;
        c.setBounds(dx, dy, d.width, d.height);
      }
    }
  }

}
