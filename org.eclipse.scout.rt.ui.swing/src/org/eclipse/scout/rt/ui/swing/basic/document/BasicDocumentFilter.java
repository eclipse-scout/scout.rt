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
package org.eclipse.scout.rt.ui.swing.basic.document;

import javax.swing.JOptionPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

public class BasicDocumentFilter extends DocumentFilter {
  private static final long serialVersionUID = 1L;

  private int m_maxLen;

  public BasicDocumentFilter() {
  }

  public BasicDocumentFilter(int maxLength) {
    m_maxLen = maxLength;
  }

  public int getMaxLength() {
    return m_maxLen;
  }

  public void setMaxLength(int maxLen) {
    m_maxLen = maxLen;
  }

  @Override
  public void insertString(FilterBypass fb, int offset, String s, AttributeSet a) throws BadLocationException {
    if (s == null) {
      s = "";
    }
    //
    Document doc = fb.getDocument();
    if (m_maxLen > 0) {
      int newLen = doc.getLength() + s.length();
      if (newLen > m_maxLen) {
        //value is too large
        s = handleStringTooLong(s, Math.max(0, s.length() - (newLen - m_maxLen)));
      }
    }
    fb.insertString(offset, s, a);
  }

  @Override
  public void replace(FilterBypass fb, int offset, int length, String s, AttributeSet a) throws BadLocationException {
    if (s == null) {
      s = "";
    }
    //
    Document doc = fb.getDocument();
    if (m_maxLen > 0) {
      int newLen = doc.getLength() + s.length() - length;
      if (newLen > m_maxLen) {
        //value is too large
        s = handleStringTooLong(s, Math.max(0, s.length() - (newLen - m_maxLen)));
      }
    }
    fb.replace(offset, length, s, a);
  }

  protected String handleStringTooLong(String s, int availableLength) throws BadLocationException {
    //ticket 89148
    if (SwingUtility.isPasteAction() || SwingUtility.isSunDropAction()) {
      SwingUtility.showMessageDialogSynthCapable(SwingUtility.getOwnerForChildWindow(), SwingUtility.getNlsText("PasteTextTooLongForFieldX", "" + getMaxLength()), SwingUtility.getNlsText("Paste"), JOptionPane.WARNING_MESSAGE);
    }
    s = s.substring(0, availableLength);
    return s;
  }
}
