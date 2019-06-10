/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;

import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/**
 * Generic Deserializer for {@link IDataObject} delegating to {@link DoEntityDeserializer} / {@link DoListDeserializer}
 * according to content.
 */
public class DataObjectDeserializer extends StdDeserializer<IDataObject> {
  private static final long serialVersionUID = 1L;

  public DataObjectDeserializer(Class<?> type) {
    super(type);
  }

  @Override
  public IDataObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return deserializeDataObject(p, ctxt, null);
  }

  @Override
  public IDataObject deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
    return deserializeDataObject(p, ctxt, typeDeserializer);
  }

  protected IDataObject deserializeDataObject(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
    switch (p.currentToken()) {
      case START_OBJECT:
        return p.getCodec().readValue(p, IDoEntity.class); // delegate to DoEntityDeserializer for object-like structure
      case START_ARRAY:
        return p.getCodec().readValue(p, DoList.class); // delegate to DoListDeserializer for collection-like structure
      default:
        throw ctxt.wrongTokenException(p, handledType(), JsonToken.START_OBJECT, "expected start object or start array token");
    }
  }
}
