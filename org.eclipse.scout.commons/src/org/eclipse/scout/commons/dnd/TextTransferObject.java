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
package org.eclipse.scout.commons.dnd;

/**
 * @since Build 202
 */

public class TextTransferObject extends TransferObject {
  private String m_plainText;
  private String m_htmlText;

  public TextTransferObject(String plainText) {
    this(plainText, null);
  }

  public TextTransferObject(String plainText, String htmlText) {
    m_plainText = plainText;
    m_htmlText = htmlText;
  }

  @Override
  public boolean isText() {
    return true;
  }

  /**
   * @deprecated use {@link TextTransferObject#getPlainText()}. Will be removed in release 3.8.0
   * @return
   */
  @Deprecated
  public String getText() {
    return getPlainText();
  }

  public String getPlainText() {
    return m_plainText;
  }

  public String getHtmlText() {
    return m_htmlText;
  }

  @Override
  public String toString() {
    return "TextTransferObject[text=" + m_plainText + ";html=" + m_htmlText + "]";
  }
}
