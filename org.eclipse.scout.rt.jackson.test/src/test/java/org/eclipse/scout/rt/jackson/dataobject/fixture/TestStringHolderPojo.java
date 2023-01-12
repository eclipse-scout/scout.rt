/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
