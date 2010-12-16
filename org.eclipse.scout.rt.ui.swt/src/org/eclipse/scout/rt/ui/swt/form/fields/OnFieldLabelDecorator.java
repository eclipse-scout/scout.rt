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
package org.eclipse.scout.rt.ui.swt.form.fields;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class OnFieldLabelDecorator implements PaintListener, FocusListener {
  private String m_text;
  private ISwtEnvironment m_env;
  private boolean m_mandatory;
  private Font m_boldFont;

  public OnFieldLabelDecorator(ISwtEnvironment env, boolean mandatory) {
    m_env = env;
    m_mandatory = mandatory;
  }

  public void setText(String s) {
    m_text = s;
  }

  public void attach(Control c) {
    if (c != null && !c.isDisposed()) {
      c.addPaintListener(this);
      c.addFocusListener(this);
    }
  }

  public void detach(Control c) {
    if (c != null && !c.isDisposed()) {
      c.removePaintListener(this);
      c.removeFocusListener(this);
    }
    if (m_boldFont != null) m_boldFont.dispose();
  }

  /*
   * Implementation
   */

  public void paintControl(PaintEvent e) {
    if (m_text == null) {
      return;
    }
    if (e.gc.isDisposed()) {
      return;
    }
    if (e.widget == null || e.widget.isDisposed()) {
      return;
    }
    Control c = (Control) e.widget;
    if (c.isFocusControl()) {
      return;
    }
    if (c instanceof Text && StringUtility.length(((Text) c).getText()) > 0) {
      return;
    }
    if (c instanceof StyledText && StringUtility.length(((StyledText) c).getText()) > 0) {
      return;
    }
    //
    e.gc.setForeground(m_env.getColor(new RGB(192, 192, 192)));
    String s = (m_text != null ? m_text : "");
    int x = c.getBorderWidth();
    int y = c.getBorderWidth();
    x += 2;
    if (m_mandatory) {
      Font f = e.gc.getFont();
      FontData[] fd = f.getFontData();
      for (FontData data : fd) {
        data.setStyle(SWT.BOLD);
      }
      if (m_boldFont == null) m_boldFont = new Font(f.getDevice(), fd);
      e.gc.setFont(m_boldFont);
    }
    e.gc.drawString(s, x, y);
  }

  public void focusGained(FocusEvent e) {
    if (e.widget instanceof Control) {
      ((Control) e.widget).redraw();
    }
  }

  public void focusLost(FocusEvent e) {
    if (e.widget instanceof Control) {
      ((Control) e.widget).redraw();
    }
  }

}
