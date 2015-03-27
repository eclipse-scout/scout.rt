/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.html.internal;

/**
 * Buffer for expressions</br>
 * (not thread safe)
 */
public abstract class AbstractExpressionBuilder implements CharSequence {
  private StringBuilder m_buf;

  private StringBuilder validate() {
    if (m_buf == null) {
      m_buf = new StringBuilder();
      build();
    }
    return m_buf;
  }

  protected abstract void build();

  @Override
  public int length() {
    return validate().length();
  }

  @Override
  public char charAt(int index) {
    return validate().charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return validate().subSequence(start, end);
  }

  @Override
  public String toString() {
    return validate().toString();
  }

  protected void append(CharSequence arg) {
    m_buf.append(arg);
  }

}
