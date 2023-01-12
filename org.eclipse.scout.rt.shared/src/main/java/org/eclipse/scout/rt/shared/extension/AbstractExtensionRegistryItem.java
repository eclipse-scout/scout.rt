/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension;

import java.io.Serializable;

public abstract class AbstractExtensionRegistryItem implements Serializable {

  private static final long serialVersionUID = 1L;
  private final long m_order;
  private final Double m_newModelOrder;

  public AbstractExtensionRegistryItem(long order, Double newModelOrder) {
    m_order = order;
    m_newModelOrder = newModelOrder;
  }

  public long getOrder() {
    return m_order;
  }

  public Double getNewModelOrder() {
    return m_newModelOrder;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_newModelOrder == null) ? 0 : m_newModelOrder.hashCode());
    result = prime * result + (int) (m_order ^ (m_order >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AbstractExtensionRegistryItem other = (AbstractExtensionRegistryItem) obj;
    if (m_newModelOrder == null) {
      if (other.m_newModelOrder != null) {
        return false;
      }
    }
    else if (!m_newModelOrder.equals(other.m_newModelOrder)) {
      return false;
    }
    if (m_order != other.m_order) {
      return false;
    }
    return true;
  }
}
