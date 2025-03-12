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

import java.util.Optional;

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IIdSignatureDataObjectMapper;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.util.LazyValue;

/**
 * Utility for wrappers of objects that are serialized using an {@link IDataObjectMapper}.
 * <p>
 * Do not use this internal class.
 *
 * @see ServiceTunnelObjectReplacer
 */
// Package-private because shouldn't be used except by ServiceTunnelObjectReplacer.
final class DataObjectWrapperUtility {

  private static final LazyValue<IDataObjectMapper> MAPPER = new LazyValue<>(IDataObjectMapper.class);
  private static final LazyValue<IIdSignatureDataObjectMapper> ID_SIGNATURE_MAPPER = new LazyValue<>(IIdSignatureDataObjectMapper.class);

  private DataObjectWrapperUtility() {
  }

  static IDataObjectMapper mapper() {
    // check the current run context for the id signature property ...
    return Optional.ofNullable(RunContext.CURRENT.get())
        .map(rc -> rc.getPropertyOrDefault(ServiceTunnelOptions.ID_SIGNATURE_PROP, false))
        // ... and return an object mapper using signatures if the property is set to true
        .map(idSignature -> idSignature ? ID_SIGNATURE_MAPPER : MAPPER)
        .orElse(MAPPER)
        .get();
  }
}
