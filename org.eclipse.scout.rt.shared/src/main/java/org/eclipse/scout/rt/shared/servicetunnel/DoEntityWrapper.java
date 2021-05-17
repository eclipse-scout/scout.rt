/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.servicetunnel;

import java.io.Serializable;

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.util.LazyValue;

/**
 * Wrapper for {@link IDoEntity} used by service tunnel.
 * <p>
 * Do not use this internal class.
 *
 * @see ServiceTunnelObjectReplacer
 */
// Package-private because shouldn't be used except by ServiceTunnelObjectReplacer.
class DoEntityWrapper implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final LazyValue<IDataObjectMapper> MAPPER = new LazyValue<>(IDataObjectMapper.class);

  // Not using transient IDoEntity along with writeObject/readObject like DoEntityHolder, because the string is read exactly once after deserialization.
  private final String m_doEntityJson;

  DoEntityWrapper(IDoEntity obj) {
    m_doEntityJson = MAPPER.get().writeValue(obj);
  }

  public IDoEntity getDoEntity() {
    return MAPPER.get().readValue(m_doEntityJson, IDoEntity.class);
  }
}
