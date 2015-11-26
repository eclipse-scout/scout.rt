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
package org.eclipse.scout.rt.client.ui.dnd;

/**
 * @since Build 202
 */

public class TextTransferObject extends TransferObject {
  private String m_plainText;

  public TextTransferObject(String plainText) {
    m_plainText = plainText;
  }

  public String getPlainText() {
    return m_plainText;
  }

  @Override
  public String toString() {
    return "TextTransferObject[text=" + m_plainText + "]";
  }
}
