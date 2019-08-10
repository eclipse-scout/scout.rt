/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

public class TestStringHolderPojo {

  private TestStringHolder m_stringHolder;

  public TestStringHolder getStringHolder() {
    return m_stringHolder;
  }

  public void setStringHolder(TestStringHolder stringHolder) {
    m_stringHolder = stringHolder;
  }

  public TestStringHolder stringHolder() {
    return m_stringHolder;
  }
}
