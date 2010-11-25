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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;

import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;

/**
 * section with property "expanded", similar to swt section
 */
public class JSection extends JPanel {
  private static final long serialVersionUID = 1L;

  private static final Color TEXT_COLOR = new Color(0x316ac5);
  private static final Color ROLLOVER_COLOR = new Color(0x7fa9f0);
  private static final Color BORDER_COLOR = new Color(0xb2cbf6);
  private static final Color BACKGROUND_COLOR = new Color(0xe5edfc);

  private JButtonEx m_button;
  private JPanelEx m_body;
  private boolean m_expandable = true;
  private boolean m_expanded = true;

  public JSection(Component comp) {
    setOpaque(false);
    m_button = new JButtonEx() {
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(Graphics g) {
        // background
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(new GradientPaint(0, 2, BACKGROUND_COLOR, 0, 20, new Color(0xffffff & BACKGROUND_COLOR.getRGB(), true), false));
        g2d.fillRect(2, 2, getWidth() - 4, getHeight() - 4);
        super.paintComponent(g);
      }
    };
    m_button.setOpaque(false);
    m_button.setFocusable(false);
    m_button.setBorder(new ButtonBorder());
    m_button.setContentAreaFilled(false);
    m_button.setHorizontalAlignment(SwingConstants.LEADING);
    m_button.setFocusPainted(false);
    m_button.setRolloverEnabled(true);
    m_button.setIcon(new ExpandedIcon(TEXT_COLOR));
    m_button.setRolloverIcon(new ExpandedIcon(ROLLOVER_COLOR));
    m_button.setForeground(TEXT_COLOR);
    m_button.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        m_button.setForeground(ROLLOVER_COLOR);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        m_button.setForeground(TEXT_COLOR);
      }
    });
    m_button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setExpanded(!isExpanded());
      }
    });
    //
    m_body = new JPanelEx(new SingleLayout());
    m_body.setOpaque(false);
    m_body.add(comp);
    //
    setLayout(new Layout());
    add(m_button);
    add(m_body);
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
      m_button.setIcon(m_expandable ? new ExpandedIcon(TEXT_COLOR) : null);
      m_button.setRolloverIcon(m_expandable ? new ExpandedIcon(ROLLOVER_COLOR) : null);
      firePropertyChange("expandable", !m_expandable, m_expandable);
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
    }
  }

  private class Layout extends AbstractLayoutManager2 {

    @Override
    protected void validateLayout(Container parent) {
    }

    @Override
    protected Dimension getLayoutSize(Container parent, int sizeflag) {
      Dimension d = new Dimension();
      d.height = 20 + 4;
      d.width = m_button.getPreferredSize().width;
      Dimension tmp = SwingLayoutUtility.getValidatedSize(m_body, sizeflag);
      // use width of box in any case
      d.width = Math.max(d.width, tmp.width);
      if (isExpanded()) {
        d.height += tmp.height;
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
      m_button.setBounds(insets.left, insets.top, size.width - insets.left - insets.right, 20);
      if (isExpanded()) {
        m_body.setBounds(insets.left, insets.top + 24, size.width - insets.left - insets.right, size.height - insets.top - insets.bottom - 24);
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

  private class ButtonBorder extends AbstractBorder {
    private static final long serialVersionUID = 1L;

    @Override
    public Insets getBorderInsets(Component c) {
      return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
      insets.top = 3;
      insets.left = 3;
      insets.bottom = 0;
      insets.right = 3;
      return insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.translate(x, y);
      g2d.setClip(new Rectangle(0, 0, width, height));
      // blue
      g2d.setPaint(new GradientPaint(0, 0, BORDER_COLOR, 0, 20, new Color(0xffffff & BORDER_COLOR.getRGB(), true), false));
      g2d.drawRoundRect(0, 0, width - 1, height + 10, 8, 8);
      // white
      g2d.setPaint(new GradientPaint(0, 0, new Color(0xffffffff, true), 0, 20, new Color(0x00ffffff, true), false));
      g2d.drawRoundRect(1, 1, width - 3, height + 10, 8, 8);
      //
      g2d.translate(-x, -y);
    }
  }

  // unit test
  // public static void main(String[] args){
  // JFrame f=new JFrame();
  // f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  // ((JComponent)f.getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
  // f.getContentPane().setBackground(Color.white);
  // //
  // JSection s1=new JSection(new JScrollPaneEx(new JTable(new DefaultTableModel())));
  // s1.setText("First");
  // JSection s2=new JSection(new JScrollPaneEx(new JTable(new DefaultTableModel())));
  // s2.setText("Second");
  // s2.setExpanded(false);
  // JSection s3=new JSection(new JScrollPaneEx(new JTable(new DefaultTableModel())));
  // s3.setText("Third");
  // //
  // Box pane=Box.createVerticalBox();
  // pane.add(s1);
  // pane.add(s2);
  // pane.add(s3);
  // f.getContentPane().add(pane, BorderLayout.CENTER);
  // f.pack();
  // f.setVisible(true);
  // }
}
