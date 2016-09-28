/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension;

public abstract class AbstractExtensionRegistryItem {

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
