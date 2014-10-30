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
import javax.swing.text.DocumentFilter;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.ui.swing.SwingUtility;

public class BasicDocumentFilter extends DocumentFilter {
  private static final long serialVersionUID = 1L;

  private int m_maxLen;

  public BasicDocumentFilter() {
    this(-1);
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
    s = StringUtility.emptyIfNull(s);
    String truncatedText = checkStringTooLong(fb, s, fb.getDocument().getLength() + s.length());
    fb.insertString(offset, truncatedText, a);
  }

  @Override
  public void replace(FilterBypass fb, int offset, int length, String s, AttributeSet a) throws BadLocationException {
    s = StringUtility.emptyIfNull(s);
    s = checkStringTooLong(fb, s, fb.getDocument().getLength() + s.length() - length);
    fb.replace(offset, length, s, a);
  }

  /**
   * Checks, if the text is too long and truncates it to the maximum length.
   *
   * @param fb
   * @param text
   *          not <code>null</code> text to check
   * @param newLength
   * @return text truncated to maximum length
   * @throws BadLocationException
   */
  protected String checkStringTooLong(FilterBypass fb, String text, int newLength) throws BadLocationException {
    if (m_maxLen > 0 && newLength > m_maxLen) {
      showTruncateTextMessage();
      return text.substring(0, Math.max(0, text.length() - (newLength - m_maxLen)));
    }
    return text;
  }

  private void showTruncateTextMessage() {
    if (SwingUtility.isPasteAction() || SwingUtility.isSunDropAction()) {
      SwingUtility.showMessageDialogSynthCapable(SwingUtility.getOwnerForChildWindow(), SwingUtility.getNlsText("PasteTextTooLongForFieldX", "" + getMaxLength()), SwingUtility.getNlsText("Paste"), JOptionPane.WARNING_MESSAGE);
    }
  }
}
