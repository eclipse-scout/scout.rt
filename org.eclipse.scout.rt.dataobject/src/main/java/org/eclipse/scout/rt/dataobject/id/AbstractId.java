/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataobject.id;

import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * Abstract base implementation for all {@link IId} classes. The wrapped id is guaranteed to be non-null.
 */
public abstract class AbstractId<WRAPPED_TYPE extends Comparable<WRAPPED_TYPE>> implements IId<WRAPPED_TYPE> {
  private static final long serialVersionUID = 1L;

  private final WRAPPED_TYPE m_id;

  protected AbstractId(WRAPPED_TYPE id) {
    m_id = Assertions.assertNotNull(id);
  }

  @Override
  public WRAPPED_TYPE unwrap() {
    return m_id;
  }

  @Override
  public String unwrapAsString() {
    return unwrap().toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
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
    AbstractId other = (AbstractId) obj;
    if (m_id == null) {
      if (other.m_id != null) {
        return false;
      }
    }
    else if (!m_id.equals(other.m_id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [" + m_id + "]";
  }
}
