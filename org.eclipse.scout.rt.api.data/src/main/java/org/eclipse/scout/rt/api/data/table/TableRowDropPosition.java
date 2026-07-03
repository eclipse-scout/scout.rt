/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.table;

import org.eclipse.scout.rt.dataobject.enumeration.EnumName;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;

@EnumName("scout.TableRowDropPosition")
public enum TableRowDropPosition implements IEnum {
  /**
   * Move source row directly before target row (same level).
   */
  BEFORE("before"),
  /**
   * Move source row directly after target row (same level).
   */
  AFTER("after"),
  /**
   * Move source row into the subtree of target row as first direct child.
   */
  FIRST_CHILD("first-child"),
  /**
   * Move source row into the subtree of target row as last direct child.
   */
  LAST_CHILD("last-child");

  private final String m_stringValue;

  TableRowDropPosition(String stringValue) {
    m_stringValue = stringValue;
  }

  @Override
  public String stringValue() {
    return m_stringValue;
  }
}
