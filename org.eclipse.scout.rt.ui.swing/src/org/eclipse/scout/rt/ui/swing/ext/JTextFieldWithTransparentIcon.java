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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup.IconState;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.HandCursorAdapater;

/**
 * This widget may be used instead of the JTextFieldWithDropDownButton when you have not enough space to display
 * text and icon, which is the case for date from/to fields where you have very limited space for displaying the date
 * text (i.e. "21.12.2010") and the date icon on the right.
 * JTextFieldWithTransparentIcon solves this problem as it allows the text to overlay the icon. Whenever this happens,
 * the icon becomes transparent for better readability.
 */
public class JTextFieldWithTransparentIcon extends JTextFieldEx {

  private static final long serialVersionUID = 1L;

  private boolean m_textOverlappingIcon = false;

  private boolean m_mouseOver = false;

  private int m_insetsRight = 3;

  private IconGroup m_iconGroup;

  private Collection<IDropDownButtonListener> m_listeners = new ArrayList<IDropDownButtonListener>();

  public JTextFieldWithTransparentIcon() {
    installDocumentListener();
    installMouseClickListener();
    installMouseListener();
    installComponentListener();
    new HandCursorAdapater(this);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    postPaintComponent(g);
  }

  /**
   * Is called after Swing component is painted to add e.g. an icon
   * 
   * @param g
   */
  protected void postPaintComponent(Graphics g) {
    Icon icon = getIconForCurrentState();
    if (icon != null) {
      // paint icon
      // only draw icon if text is smaller than available width
      if (!isTextOverlappingIcon()) {
        int xPos = getWidth() - icon.getIconWidth() - getInsetsRight();
        int yPos = (getHeight() - icon.getIconHeight()) / 2;
        icon.paintIcon(this, g, xPos, yPos);
      }
    }
  }

  private void installComponentListener() {
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        updateTextOverlappingIcon();
      }
    });
  }

  private void installMouseClickListener() {
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (!isIconEnabled()) {
          return;
        }
        for (IDropDownButtonListener l : m_listeners) {
          l.iconClicked(e.getSource());
        }
      }
    });
  }

  private void installDocumentListener() {
    getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        updateTextOverlappingIcon();
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        updateTextOverlappingIcon();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updateTextOverlappingIcon();
      }
    });
  }

  public void installMouseListener() {
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        updateIcon(true);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        updateIcon(false);
      }

      void updateIcon(boolean mouseOver) {
        if (!isIconEnabled()) {
          return;
        }
        boolean oldMouseOver = m_mouseOver;
        if (oldMouseOver != mouseOver) {
          m_mouseOver = mouseOver;
          repaint();
        }
      }
    });
  }

  private void updateTextOverlappingIcon() {
    boolean oldTextOverlappingIcon = m_textOverlappingIcon;
    if (!m_iconGroup.hasIcon(IconState.NORMAL)) {
      m_textOverlappingIcon = false;
    }
    else {
      int textWidth = calcTextWidth();
      int iconWidth = m_iconGroup.getIcon(IconState.NORMAL).getIconWidth();
      int fieldWidth = getSize().width;
      int insetsLeft = getInsets().left;
      int customInsetsRigth = getInsetsRight();
      m_textOverlappingIcon = (textWidth > fieldWidth - iconWidth - insetsLeft - customInsetsRigth);
    }
    if (oldTextOverlappingIcon != m_textOverlappingIcon) {
      repaint();
    }
  }

  private int calcTextWidth() {
    String text = getText();
    Font f = getFont();
    FontMetrics fm = getFontMetrics(f);
    Rectangle2D bounds = fm.getStringBounds(text, getGraphics());
    return (int) bounds.getWidth();
  }

  private boolean isIconEnabled() {
    return isEnabled() && isEditable();
  }

  protected Icon getIconForCurrentState() {
    Icon icon = m_iconGroup.getIcon(IconState.NORMAL);
    if (!isIconEnabled() && m_iconGroup.hasIcon(IconState.DISABLED)) {
      icon = m_iconGroup.getIcon(IconState.DISABLED);
    }
    else if (m_mouseOver && m_iconGroup.hasIcon(IconState.ROLLOVER)) {
      icon = m_iconGroup.getIcon(IconState.ROLLOVER);
    }
    // use the SELECTED state of the IconGroup for our 'transparent' icon.
    else if (isTextOverlappingIcon() && m_iconGroup.hasIcon(IconState.SELECTED)) {
      icon = m_iconGroup.getIcon(IconState.SELECTED);
    }
    return icon;
  }

  protected boolean isTextOverlappingIcon() {
    return m_textOverlappingIcon;
  }

  public int getInsetsRight() {
    return m_insetsRight;
  }

  public void setInsetsRight(int insetsRight) {
    m_insetsRight = insetsRight;
  }

  public void setIconGroup(IconGroup iconGroup) {
    m_iconGroup = iconGroup;
    updateTextOverlappingIcon();
  }

  public void addDropDownButtonListener(IDropDownButtonListener l) {
    m_listeners.add(l);
  }

  public void removeDropDownButtonListener(IDropDownButtonListener l) {
    m_listeners.remove(l);
  }

}
