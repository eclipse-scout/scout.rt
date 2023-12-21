/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.id;

import static java.util.Collections.unmodifiableSet;

import java.util.Set;

import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.dataobject.id.IdCodec.IIdCodecFlag;
import org.eclipse.scout.rt.jackson.dataobject.ScoutDataObjectModuleContext;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Abstract {@link StdDeserializer} with {@link ScoutDataObjectModuleContext} that provides an {@link IdCodec} and
 * information about the {@link IIdCodecFlag}s of the context.
 */
public abstract class AbstractIdCodecDeserializer<T> extends StdDeserializer<T> {
  private static final long serialVersionUID = 1L;

  protected final ScoutDataObjectModuleContext m_moduleContext;

  protected final LazyValue<IdCodec> m_idCodec = new LazyValue<>(IdCodec.class);
  protected final LazyValue<Set<IIdCodecFlag>> m_idCodecFlags = new LazyValue<>(() -> unmodifiableSet(computeIdCodecFlags()));

  public AbstractIdCodecDeserializer(ScoutDataObjectModuleContext moduleContext, Class<?> valueClass) {
    super(valueClass);
    m_moduleContext = moduleContext;
  }

  public AbstractIdCodecDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType valueType) {
    super(valueType);
    m_moduleContext = moduleContext;
  }

  protected ScoutDataObjectModuleContext moduleContext() {
    return m_moduleContext;
  }

  protected IdCodec idCodec() {
    return m_idCodec.get();
  }

  protected Set<IIdCodecFlag> computeIdCodecFlags() {
    return IdCodecUtility.getIdCodecFlags(moduleContext());
  }

  protected Set<IIdCodecFlag> idCodecFlags() {
    return m_idCodecFlags.get();
  }
}
