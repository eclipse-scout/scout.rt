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
package org.eclipse.scout.commons.html;

/**
 *
 */
public class HtmlBind implements IHtmlContent {

  private final String m_name;

  public HtmlBind(String name) {
    m_name = name;
  }

  @Override
  public int length() {
    return m_name.length();
  }

  @Override
  public char charAt(int index) {
    return m_name.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return m_name.subSequence(end, end);
  }

  @Override
  public String toString() {
    return m_name;
  }

}
