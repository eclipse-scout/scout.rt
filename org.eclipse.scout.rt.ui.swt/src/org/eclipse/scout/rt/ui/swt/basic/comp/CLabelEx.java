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
package org.eclipse.scout.rt.ui.swt.basic.comp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

/**
 * This class overrides the text shortening behaviour of the CLabel
 * class.Instead of replacing the center of the text with an ellipsis, this
 * class adds an ellipsis at the end of the text (or before the last word of the
 * text, if the label is part of a field range composition).
 */
public class CLabelEx extends CLabel {

  private static final int DRAW_FLAGS = SWT.DRAW_MNEMONIC | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER;
  private static final String ELLIPSIS = "...";
  private boolean m_isFieldRangeLabel = false;

  public CLabelEx(Composite parent, int style) {
    super(parent, style);
  }

  public boolean isFieldRangeLabel() {
    return m_isFieldRangeLabel;
  }

  public void setFieldRangeLabel(boolean isFieldRangeLabel) {
    this.m_isFieldRangeLabel = isFieldRangeLabel;
  }

  @Override
  protected String shortenText(GC gc, String t, int labelWidth) {
    if (t == null) {
      return null;
    }
    String text = t;
    String rangeWord = "";
    if (m_isFieldRangeLabel) {
      text = t.substring(0, t.lastIndexOf(' '));
      rangeWord = t.substring(t.lastIndexOf(' '));
    }
    int textWidth = gc.textExtent(ELLIPSIS, DRAW_FLAGS).x;
    int rangeWidth = gc.textExtent(rangeWord, DRAW_FLAGS).x;
    // initial number of characters
    int s = text.length();
    // shorten string
    while (s >= 0) {
      String s1 = t.substring(0, s);
      int l1 = gc.textExtent(s1, DRAW_FLAGS).x;
      if (l1 + textWidth + rangeWidth < labelWidth) {
        t = s1 + ELLIPSIS + rangeWord;
        break;
      }
      s--;
    }
    return t;
  }
}
