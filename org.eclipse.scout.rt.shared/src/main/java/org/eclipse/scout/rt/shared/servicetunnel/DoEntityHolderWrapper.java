/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel;

import static org.eclipse.scout.rt.shared.servicetunnel.DataObjectWrapperUtility.mapper;

import java.io.Serial;
import java.io.Serializable;

import org.eclipse.scout.rt.dataobject.DoEntityHolder;
import org.eclipse.scout.rt.dataobject.IDoEntity;

/**
 * Wrapper for {@link DoEntityHolder} used by service tunnel.
 * <p>
 * Do not use this internal class.
 *
 * @see ServiceTunnelObjectReplacer
 */
// Package-private because shouldn't be used except by ServiceTunnelObjectReplacer.
class DoEntityHolderWrapper implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  // Not using transient DoEntityHolder along with writeObject/readObject like DoEntityHolder, because the string is read exactly once after deserialization.
  private final String m_doEntityJson;

  DoEntityHolderWrapper(DoEntityHolder obj) {
    m_doEntityJson = mapper().writeValue(obj.getValue());
  }

  public DoEntityHolder<? extends IDoEntity> getDoEntityHolder() {
    return new DoEntityHolder<>(null, mapper().readValue(m_doEntityJson, IDoEntity.class));
  }
}
