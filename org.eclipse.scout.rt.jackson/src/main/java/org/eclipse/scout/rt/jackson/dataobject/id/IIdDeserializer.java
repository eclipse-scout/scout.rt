/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject.id;

import java.io.IOException;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdFactory;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom deserializer for {@link IId} values.
 */
public class IIdDeserializer extends StdDeserializer<IId<?>> {
  private static final long serialVersionUID = 1L;

  protected final Class<? extends IId<?>> m_idType;
  protected final LazyValue<IdFactory> m_idFactory = new LazyValue<>(IdFactory.class);

  public IIdDeserializer(Class<? extends IId<?>> idType) {
    super(idType);
    m_idType = idType;
  }

  @Override
  public IId<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    Class<?> wtClass = TypeCastUtility.getGenericsParameterClass(m_idType, IId.class);
    return m_idFactory.get().createInternal(m_idType, p.readValueAs(wtClass));
  }
}
