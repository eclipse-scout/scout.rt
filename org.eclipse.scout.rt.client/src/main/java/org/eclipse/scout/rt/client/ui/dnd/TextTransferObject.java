/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.dnd;

/**
 * @since Build 202
 */

public class TextTransferObject extends TransferObject {
  private final String m_plainText;

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
