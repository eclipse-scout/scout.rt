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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link ICompositeId} interface to represent an arbitrary id based on the composition of
 * multiple {@link IId}'s as components. The type(s) of the raw (wrapped) ids is required to be an instance of
 * {@link IId}.
 */
@IdSignature
public abstract class AbstractCompositeId implements ICompositeId {
  private static final long serialVersionUID = 1L;

  private final List<? extends IId> m_idComponents;

  protected AbstractCompositeId(IId... idComponents) {
    if (idComponents == null || idComponents.length == 0) {
      throw new IllegalArgumentException("idComponents is null");
    }
    m_idComponents = Arrays.asList(idComponents);
  }

  @Override
  public List<? extends IId> unwrap() {
    return Collections.unmodifiableList(m_idComponents);
  }

  /**
   * @return component with given {@code index} converted to its type {@code ID}
   */
  @SuppressWarnings("unchecked")
  protected final <ID extends IId> ID idComponent(int index) {
    return (ID) m_idComponents.get(index);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AbstractCompositeId compositeId = (AbstractCompositeId) o;
    if (!m_idComponents.equals(compositeId.m_idComponents)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return m_idComponents.hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + m_idComponents;
  }
}
