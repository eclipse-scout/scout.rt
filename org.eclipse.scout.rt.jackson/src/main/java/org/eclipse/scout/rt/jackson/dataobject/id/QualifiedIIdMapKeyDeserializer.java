/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.id;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdExternalFormatter;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

/**
 * Custom deserializer for {@link IId} instances - like {@link TypedIdDeserializer} it uses {@link IdExternalFormatter}
 * for serialization. It may be used as a replacement for {@link IIdDeserializer}.
 */
public class QualifiedIIdMapKeyDeserializer extends KeyDeserializer {

  protected final LazyValue<IdExternalFormatter> m_idExternalFormatter = new LazyValue<>(IdExternalFormatter.class);

  @Override
  public Object deserializeKey(String key, DeserializationContext ctxt) {
    return m_idExternalFormatter.get().fromExternalForm(key);
  }
}
