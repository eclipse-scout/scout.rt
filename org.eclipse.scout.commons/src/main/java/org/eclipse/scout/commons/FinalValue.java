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
package org.eclipse.scout.commons;

/**
 *
 */
public class FinalValue<VALUE> {

  private VALUE m_value;

  public FinalValue() {
  }

  public FinalValue(VALUE value) {
    m_value = value;
  }

  public void setValue(VALUE value) {
    Assertions.assertNull(m_value, String.format("%s's can only be set once.", getClass().getSimpleName()));
    m_value = value;
  }

  public VALUE getValue() {
    return m_value;
  }
}
