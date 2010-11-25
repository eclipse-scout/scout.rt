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
  private String m_text;

  public TextTransferObject(String s) {
    m_text = s;
  }

  @Override
  public boolean isText() {
    return true;
  }

  public String getText() {
    return m_text;
  }

  @Override
  public String toString() {
    return "TextTransferObject[text=" + m_text + "]";
  }
}
