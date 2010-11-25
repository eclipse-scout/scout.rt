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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingIcons;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup.IconState;
import org.eclipse.scout.rt.ui.swing.ext.JLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;

public class CollapseButton extends JPanelEx {
  private static final long serialVersionUID = 1L;

  private final ISwingEnvironment m_env;
  private JLabelEx m_label;
  private JLabelEx m_metricsLabel;
  private IconGroup m_arrowIcon;
  private Collection<String> m_potentialTexts;

  CollapseButton(ISwingEnvironment env) {
    m_env = env;
    setLayout(new Layout());
    setOpaque(false);
    m_label = new JLabelEx();
    add(m_label);
    m_label.setBorder(null);
    //addIcon
    m_arrowIcon = new IconGroup(env, SwingIcons.IconSlider);
    m_label.setIcon(m_arrowIcon.getIcon(IconState.NORMAL));
    m_label.setIconTextGap(7);
    //addTitle
    m_label.setBold(true);
    m_label.setForeground(Color.WHITE);
    //add invisible metrics label
    m_metricsLabel = new JLabelEx();
    m_metricsLabel.setIcon(m_arrowIcon.getIcon(IconState.NORMAL));
    m_metricsLabel.setIconTextGap(7);
    m_metricsLabel.setBold(true);
    m_metricsLabel.setBorder(null);
    m_metricsLabel.setVisible(false);
    //do NOT add metricsLabel to the container, this would yield to to a loop when validating the container
    //
    new HandCursorAdapater(this);
    installListeners();
  }

  public void setText(String text) {
    m_label.setText(text);
  }

  /**
   * Set all potential texts that may be displayed.
   * <p>
   * This enables calculation of minimum/preferred size based on the longest possible text on the label.
   */
  public void setPotentialTexts(Collection<String> texts) {
    m_potentialTexts = texts;
    revalidate();
  }

  private void installListeners() {
    m_label.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        m_label.setIcon(m_arrowIcon.getIcon(IconState.NORMAL));
        setVisible(false);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        m_label.setIcon(m_arrowIcon.getIcon(IconState.ROLLOVER));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        m_label.setIcon(m_arrowIcon.getIcon(IconState.NORMAL));
      }
    });
  }

  private class Layout extends AbstractLayoutManager2 {
    private Dimension m_size;

    @Override
    protected void validateLayout(Container parent) {
      Dimension d = new Dimension();
      if (m_potentialTexts != null) {
        for (String s : m_potentialTexts) {
          m_metricsLabel.setText(s);
          Dimension t = SwingLayoutUtility.getPreferredLabelSize(m_metricsLabel, 10240);
          d.width = Math.max(d.width, t.width);
          d.height = Math.max(d.height, t.height);
        }
      }
      else {
        d = SwingLayoutUtility.getPreferredLabelSize(m_label, 10240);
      }
      Insets insets = parent.getInsets();
      d.width += insets.left + insets.right;
      d.height += insets.top + insets.bottom;
      m_size = d;
    }

    @Override
    protected Dimension getLayoutSize(Container parent, int sizeflag) {
      return m_size;
    }

    @Override
    public void layoutContainer(Container parent) {
      Insets insets = parent.getInsets();
      m_label.setBounds(new Rectangle(insets.left, insets.top, parent.getWidth() - insets.left - insets.right, parent.getHeight() - insets.top - insets.bottom));
    }
  }
}
