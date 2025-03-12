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

import java.io.Serializable;

import org.eclipse.scout.rt.dataobject.DataObjectHolder;
import org.eclipse.scout.rt.dataobject.IDataObject;

/**
 * Wrapper for {@link DataObjectHolder} used by service tunnel.
 * <p>
 * Do not use this internal class.
 *
 * @see ServiceTunnelObjectReplacer
 */
// Package-private because shouldn't be used except by ServiceTunnelObjectReplacer.
class DataObjectHolderWrapper implements Serializable {
  private static final long serialVersionUID = 1L;

  // Not using transient DataObjectHolder along with writeObject/readObject like DataObjectHolder, because the string is read exactly once after deserialization.
  private final String m_dataObjectJson;

  DataObjectHolderWrapper(DataObjectHolder obj) {
    m_dataObjectJson = mapper().writeValue(obj.getValue());
  }

  public DataObjectHolder<? extends IDataObject> getDataObjectHolder() {
    return new DataObjectHolder<>(null, mapper().readValue(m_dataObjectJson, IDataObject.class));
  }
}
