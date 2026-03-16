/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.io.Serial;
import java.util.EventObject;
import java.util.Objects;

import org.eclipse.scout.rt.client.ui.IModelEvent;

public class SearchOutlineEvent extends EventObject implements IModelEvent {

  @Serial
  private static final long serialVersionUID = 1L;

  public static final int TYPE_SEARCH_EVENT = 7;

  private final int m_type;

  protected SearchOutlineEvent(Object source, int type) {
    super(source);
    m_type = type;
  }

  @Override
  public int getType() {
    return m_type;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchOutlineEvent that = (SearchOutlineEvent) o;
    return m_type == that.m_type;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(m_type);
  }

  @Override
  public String toString() {
    return "SearchOutlineEvent ["
        + "source=" + source + ", "
        + "type=" + m_type
        + "]";
  }
}
