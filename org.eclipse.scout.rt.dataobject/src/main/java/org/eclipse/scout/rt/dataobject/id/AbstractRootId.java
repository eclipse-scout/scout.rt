/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * Abstract base implementation for all {@link IId} classes. The wrapped id is guaranteed to be non-null.
 */
public abstract class AbstractRootId<WRAPPED_TYPE> implements IRootId {
  private static final long serialVersionUID = 1L;

  private final WRAPPED_TYPE m_id;

  protected AbstractRootId(WRAPPED_TYPE id) {
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
    return m_id.hashCode();
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
    AbstractRootId other = (AbstractRootId) obj;
    return m_id.equals(other.m_id);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [" + m_id + "]";
  }
}
