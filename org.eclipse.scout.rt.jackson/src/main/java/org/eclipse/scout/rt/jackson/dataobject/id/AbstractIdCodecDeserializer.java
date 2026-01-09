/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.id;

import java.util.Collections;
import java.util.Set;

import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.dataobject.id.IdCodec.IIdCodecFlag;
import org.eclipse.scout.rt.jackson.dataobject.ScoutDataObjectModuleContext;
import org.eclipse.scout.rt.platform.BEANS;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Abstract {@link StdDeserializer} with {@link ScoutDataObjectModuleContext} that provides an {@link IdCodec} and
 * information about the {@link IIdCodecFlag}s of the context.
 */
public abstract class AbstractIdCodecDeserializer<T> extends StdDeserializer<T> {
  private static final long serialVersionUID = 1L;

  protected final IdCodec m_idCodec = BEANS.get(IdCodec.class);
  protected final ScoutDataObjectModuleContext m_moduleContext;
  protected final Set<IIdCodecFlag> m_idCodecFlags;

  public AbstractIdCodecDeserializer(ScoutDataObjectModuleContext moduleContext, Class<?> valueClass) {
    super(valueClass);
    m_moduleContext = moduleContext;
    m_idCodecFlags = Collections.unmodifiableSet(computeIdCodecFlags());
  }

  public AbstractIdCodecDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType valueType) {
    super(valueType);
    m_moduleContext = moduleContext;
    m_idCodecFlags = Collections.unmodifiableSet(computeIdCodecFlags());
  }

  protected ScoutDataObjectModuleContext moduleContext() {
    return m_moduleContext;
  }

  protected IdCodec idCodec() {
    return m_idCodec;
  }

  protected Set<IIdCodecFlag> computeIdCodecFlags() {
    return IdCodecUtility.getIdCodecFlags(moduleContext());
  }

  protected Set<IIdCodecFlag> idCodecFlags() {
    return m_idCodecFlags;
  }
}
