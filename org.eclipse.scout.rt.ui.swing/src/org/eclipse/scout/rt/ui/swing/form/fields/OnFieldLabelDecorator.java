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
package org.eclipse.scout.rt.ui.swing.form.fields;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.CompareUtility;

/**
 * Presents the label of a field on the label itself when it is not focused and has no content yet
 */
public class OnFieldLabelDecorator {
  private JComponent m_component;
  private FocusListener m_focusListener;
  private String m_label;
  private boolean m_mandatory;

  public OnFieldLabelDecorator(JComponent c, boolean mandatory) {
    m_component = c;
    m_mandatory = mandatory;
    m_focusListener = new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        m_component.repaint();
      }

      @Override
      public void focusLost(FocusEvent e) {
        m_component.repaint();
      }
    };
    m_component.addFocusListener(m_focusListener);
  }

  public String getLabel() {
    return m_label;
  }

  public void setLabel(String s) {
    if (!CompareUtility.equals(m_label, s)) {
      m_label = s;
      m_component.repaint();
    }
  }

  public void paintOnFieldLabel(Graphics g, JComponent field) {
    if (field == null) {
      return;
    }
    if (field.hasFocus()) {
      return;
    }
    if (field instanceof JTextComponent) {
      if (((JTextComponent) field).getDocument().getLength() > 0) {
        return;
      }
    }
    if (m_label != null && m_label.length() > 0) {
      int baseline = field.getBaseline(field.getWidth(), field.getHeight());
      g.setColor(Color.lightGray);
      if (m_mandatory) {
        Font f = field.getFont().deriveFont(Font.BOLD);
        g.setFont(f);
      }
      g.drawString(m_label, field.getInsets().left, baseline);
    }
  }
}
