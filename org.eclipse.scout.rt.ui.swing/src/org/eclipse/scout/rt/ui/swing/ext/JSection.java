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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;

/**
 * A Section component with property "expanded".
 * <p>
 * Similar to the swt Section component.
 * <p>
 * The component consists of a button (line) and a body. The button is transparented out on the bottom when the section
 * is expanded.
 */
public class JSection extends JPanel {
  private static final long serialVersionUID = 1L;
  private static final int VERTICAL_GAP = 2;

  private final JButton m_button;
  private final JPanel m_head;
  private final JPanel m_body;
  private boolean m_expandable = true;
  private boolean m_expanded = true;

  public JSection(Component comp) {
    setOpaque(false);
    m_button = createButton();
    m_button.setIcon(new ExpandedIcon(m_button.getForeground()));
    m_button.setRolloverIcon(new ExpandedIcon(m_button.getForeground()));
    m_button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setExpanded(!isExpanded());
      }
    });
    //
    m_head = new JPanelEx(new HeadLayout());
    m_head.setOpaque(false);
    m_head.add(m_button);
    //
    m_body = new JPanelEx(new SingleLayout());
    m_body.setOpaque(false);
    m_body.add(comp);
    //
    setLayout(new SectionLayout());
    add(m_head);
    add(m_body);
  }

  protected JButton createButton() {
    JButton button = new SectionButton();
    button.setVerticalAlignment(SwingConstants.TOP);
    button.setHorizontalAlignment(SwingConstants.LEADING);
    button.setFocusPainted(false);
    return button;
  }

  public JComponent getContentPane() {
    return m_body;
  }

  public String getText() {
    return m_button.getText();
  }

  public void setText(String s) {
    m_button.setText(s);
  }

  public boolean isExpandable() {
    return m_expandable;
  }

  public void setExpandable(boolean expandable) {
    if (m_expandable != expandable) {
      m_expandable = expandable;
      m_button.setIcon(m_expandable ? new ExpandedIcon(m_button.getForeground()) : null);
      m_button.setRolloverIcon(m_expandable ? new ExpandedIcon(m_button.getForeground()) : null);
      firePropertyChange("expandable", !m_expandable, m_expandable);
      repaint();
    }
  }

  public boolean isExpanded() {
    return m_expanded;
  }

  public void setExpanded(boolean expanded) {
    if (expanded != m_expanded) {
      m_expanded = expanded;
      m_body.setVisible(m_expanded);
      firePropertyChange("expanded", !m_expanded, m_expanded);
      m_head.revalidate();
      repaint();
    }
  }

  private class SectionButton extends JButtonEx {
    private static final long serialVersionUID = 1L;

    @Override
    protected void paintComponent(Graphics g) {
      if (isExpanded()) {
        int w = getWidth();
        int h = getHeight();
        int expandedExcessHeight = getInsets().bottom;
        Rectangle oldClip = g.getClipBounds();
        Rectangle narrowedClip = oldClip.intersection(new Rectangle(0, 0, w, h - expandedExcessHeight));
        g.setClip(narrowedClip);
        super.paintComponent(g);
        g.setClip(oldClip);
        Graphics2D g2d = (Graphics2D) g;
        //If expanded, cut off the second bottom part. Layout added an extra bottom part to cut off.
        // find bg color
        Component tmp = getParent();
        while (tmp != null && !tmp.isOpaque()) {
          tmp = tmp.getParent();
        }
        int backgroundRGB = (tmp != null ? tmp.getBackground() : Color.white).getRGB() & 0xffffff;
        // clear area
        for (int y = 0; y <= expandedExcessHeight; y++) {
          int f = Math.min(0xff, 0x100 * y / expandedExcessHeight);
          g2d.setColor(new Color((0x01000000 * f) | backgroundRGB, true));
          g2d.drawLine(0, h - expandedExcessHeight - expandedExcessHeight + y, w, h - expandedExcessHeight - expandedExcessHeight + y);
        }
        g2d.setColor(new Color(0xff000000 | backgroundRGB, true));
        g2d.fillRect(0, h - expandedExcessHeight, w, expandedExcessHeight);
        //narrow clip again
        g.setClip(narrowedClip);
      }
      else {
        super.paintComponent(g);
      }
    }
  }

  /**
   * Layout head and body panel
   */
  private class SectionLayout extends AbstractLayoutManager2 {
    @Override
    protected void validateLayout(Container parent) {
    }

    @Override
    protected Dimension getLayoutSize(Container parent, int sizeflag) {
      Dimension d = new Dimension();
      Dimension headSize = m_head.getPreferredSize();
      Dimension bodySize = SwingLayoutUtility.getValidatedSize(m_body, sizeflag);
      d.width = Math.max(headSize.width, bodySize.width);
      d.height = headSize.height;
      if (isExpanded()) {
        d.height += 4;
        d.height += bodySize.height;
      }
      Insets insets = parent.getInsets();
      d.width += insets.left + insets.right;
      d.height += insets.top + insets.bottom;
      return d;
    }

    @Override
    public void layoutContainer(Container parent) {
      Insets insets = parent.getInsets();
      Dimension size = parent.getSize();
      int headHeight = m_head.getPreferredSize().height;
      m_head.setBounds(insets.left, insets.top, size.width - insets.left - insets.right, headHeight);
      if (isExpanded()) {
        m_body.setBounds(insets.left, insets.top + 4 + headHeight, size.width - insets.left - insets.right, size.height - insets.top - insets.bottom - 4 - headHeight);
      }
    }
  }

  /**
   * Layout head button with excess space for border manipulationa and fade-out
   */
  private class HeadLayout extends AbstractLayoutManager2 {
    @Override
    protected void validateLayout(Container parent) {
    }

    @Override
    protected Dimension getLayoutSize(Container parent, int sizeflag) {
      Dimension d = new Dimension();
      Dimension buttonSize = m_button.getPreferredSize();
      d.width = buttonSize.width;
      d.height = buttonSize.height;
      Insets insets = parent.getInsets();
      d.width += insets.left + insets.right;
      d.height += insets.top + insets.bottom;
      return d;
    }

    @Override
    public void layoutContainer(Container parent) {
      Insets insets = parent.getInsets();
      Dimension size = parent.getSize();
      int buttonHeight = m_button.getPreferredSize().height;
      if (isExpanded()) {
        //make button larger by excess height to cut it off
        int expandedExcessHeight = m_button.getInsets().bottom;
        m_button.setBounds(insets.left, insets.top, size.width - insets.left - insets.right, buttonHeight + expandedExcessHeight);
      }
      else {
        m_button.setBounds(insets.left, insets.top, size.width - insets.left - insets.right, buttonHeight);
      }
    }
  }

  private class ExpandedIcon implements Icon {
    private static final long serialVersionUID = 1L;

    private Color m_color;

    public ExpandedIcon(Color col) {
      m_color = col;
    }

    @Override
    public int getIconWidth() {
      return 16;
    }

    @Override
    public int getIconHeight() {
      return 16;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(m_color);
      if (isExpanded()) {
        int dx = 4;
        int dy = 6;
        int w = 8;
        for (int i = 0; i < 4; i++) {
          g.drawLine(x + dx, y + dy, x + dx + w - 1, y + dy);
          dx += 1;
          dy += 1;
          w -= 2;
        }
      }
      else {
        int dx = 6;
        int dy = 4;
        int w = 8;
        for (int i = 0; i < 4; i++) {
          g.drawLine(x + dx, y + dy, x + dx, y + dy + w - 1);
          dx += 1;
          dy += 1;
          w -= 2;
        }
      }
    }
  }

}
