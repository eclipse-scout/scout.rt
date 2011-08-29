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
package org.eclipse.scout.rt.ui.swing.form.fields.tabbox;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.form.fields.groupbox.ISwingScoutGroupBox;

public class SwingScoutTabItem extends SwingScoutComposite<IGroupBox> implements ISwingScoutTabItem {
  private Icon m_swingTabIcon;
  private ISwingScoutGroupBox m_groupBoxComposite;
  private static final Color FOREGROUND = UIManager.getColor("TabItem.foreground");
  private static final Color SELECTED = UIManager.getColor("TabItem.selected.foreground");
  private static final Color DISABLED = UIManager.getColor("textInactiveText");

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    m_groupBoxComposite = (ISwingScoutGroupBox) getSwingEnvironment().createFormField(null, getScoutObject());
    m_swingTabIcon = createSwingTabIcon();
    setSwingField(m_groupBoxComposite.getSwingContainer());
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IGroupBox scoutField = getScoutObject();
    if (scoutField != null) {
      setSaveNeededFromScout();
      setEmptyFromScout();
      setLabelFromScout();
      setFontFromScout();
    }
  }

  protected SwingTabIcon createSwingTabIcon() {
    return new SwingTabIcon();
  }

  @Override
  public Icon getSwingTabIcon() {
    return m_swingTabIcon;
  }

  @Override
  public ISwingScoutGroupBox getGroupBoxComposite() {
    return m_groupBoxComposite;
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (IGroupBox.PROP_EMPTY.equals(name)) {
      setEmptyFromScout();
    }
    else if (IGroupBox.PROP_SAVE_NEEDED.equals(name)) {
      setSaveNeededFromScout();
    }
    else if (IGroupBox.PROP_LABEL.equals(name)) {
      setLabelFromScout();
    }
    else if (IGroupBox.PROP_FONT.equals(name)) {
      setFontFromScout();
    }
  }

  protected void setSaveNeededFromScout() {
    if (getScoutObject().getForm() instanceof ISearchForm) {
      if (getSwingTabIcon() instanceof SwingTabIcon) {
        ((SwingTabIcon) getSwingTabIcon()).setMarked(getScoutObject().isSaveNeeded());
      }
      repaintSwingTabbedPane();
    }
  }

  protected void setEmptyFromScout() {
    if (!(getScoutObject().getForm() instanceof ISearchForm)) {
      if (getSwingTabIcon() instanceof SwingTabIcon) {
        ((SwingTabIcon) getSwingTabIcon()).setMarked(!getScoutObject().isEmpty());
      }
      repaintSwingTabbedPane();
    }
  }

  protected void setLabelFromScout() {
    if (getSwingTabIcon() instanceof SwingTabIcon) {
      String s = getScoutObject().getLabel();
      if (s == null) {
        s = "";
      }
      String label = StringUtility.removeMnemonic(s);
      ((SwingTabIcon) getSwingTabIcon()).setText(label);
    }
    repaintSwingTabbedPane();
  }

  protected void setFontFromScout() {
    repaintSwingTabbedPane();
  }

  private void repaintSwingTabbedPane() {
    JTabbedPane parent = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, getSwingContainer());
    if (parent != null) {
      parent.revalidate();
      parent.repaint();
    }
  }

  public class SwingTabIcon implements Icon {
    private static final int TEXT_ICON_GAP = 2;

    private Insets m_insets;
    // text should never be null!
    private String m_text = "";
    private boolean m_marked = false;
    private boolean m_selected = false;
    private Color m_markerColor = new Color(0xFFAA40);
    private Icon m_markerIcon;

    public SwingTabIcon() {
      m_insets = UIManager.getInsets("TabbedPane.tabAreaInsets");
      if (m_insets == null) {
        m_insets = new Insets(0, 0, 0, 0);
      }
    }

    public void setText(String s) {
      m_text = s != null ? s : "";
    }

    public String getText() {
      return m_text;
    }

    public void setMarked(boolean b) {
      m_marked = b;
    }

    public boolean isMarked() {
      return m_marked;
    }

    public void setMarkerIcon(Icon icon) {
      m_markerIcon = icon;
    }

    public Icon getMarkerIcon() {
      return m_markerIcon;
    }

    public void setSelected(boolean selected) {
      m_selected = selected;
    }

    @Override
    public int getIconWidth() {
      Font f = getSwingContainer().getFont();
      if (f != null) {
        int w = getSwingContainer().getFontMetrics(f).stringWidth(m_text);
        if (getMarkerIcon() != null) {
          w = w + (isMarked() ? (TEXT_ICON_GAP + getMarkerIcon().getIconWidth()) : 0);
        }
        return w;
      }
      else {
        return 100;
      }
    }

    @Override
    public int getIconHeight() {
      Font f = getSwingContainer().getFont();
      if (f != null) {
        return getSwingContainer().getFontMetrics(f).getHeight() + m_insets.top + m_insets.bottom;
      }
      else {
        return 16 + m_insets.top + m_insets.bottom;
      }
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Font f = getSwingContainer().getFont();
      FontMetrics fm = getSwingContainer().getFontMetrics(f);
      g.setFont(f);
      int baseline = y + m_insets.top + fm.getAscent();
      String s = m_text;
      if (m_marked) {
        int w = fm.stringWidth(s) - 1;
        if (getMarkerIcon() != null) {
          // append marker icon when marked
          getMarkerIcon().paintIcon(c, g, x + w + TEXT_ICON_GAP, baseline - getMarkerIcon().getIconHeight());
        }
        else {
          // underline if marked
          g.setColor(m_markerColor);
          g.drawLine(x, baseline + 1, x + w, baseline + 1);
          g.drawLine(x, baseline + 2, x + w, baseline + 2);
        }
      }
      // text
      if (!getScoutObject().isEnabled()) {
        g.setColor(DISABLED);
      }
      else if (m_selected) {
        g.setColor(SELECTED);
      }
      else {
        g.setColor(FOREGROUND);
      }
      g.drawString(s, x, baseline);
    }
  }// end class

}
