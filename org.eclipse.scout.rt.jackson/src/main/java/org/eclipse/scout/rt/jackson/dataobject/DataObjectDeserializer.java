/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.math.BigDecimal;

import org.eclipse.scout.rt.dataobject.BigDecimalDataObjectValue;
import org.eclipse.scout.rt.dataobject.BooleanDataObjectValue;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.LongDataObjectValue;
import org.eclipse.scout.rt.dataobject.StringDataObjectValue;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/**
 * Generic Deserializer for {@link IDataObject} delegating to {@link DoEntityDeserializer} /
 * {@link DoCollectionDeserializer} according to content.
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
    return switch (p.currentToken()) {
      case START_OBJECT -> p.getCodec().readValue(p, IDoEntity.class); // delegate to DoEntityDeserializer for object-like structure
      case START_ARRAY -> p.getCodec().readValue(p, DoList.class); // delegate to DoCollectionDeserializer for collection-like structure (using DoList as generic structure instead of DoSet or DoCollection)
      case VALUE_STRING -> new StringDataObjectValue().withValue(p.getValueAsString());
      case VALUE_NUMBER_INT -> new LongDataObjectValue().withValue(p.getValueAsLong());
      case VALUE_NUMBER_FLOAT -> new BigDecimalDataObjectValue().withValue(p.getCodec().readValue(p, BigDecimal.class)); // deserialize floating point numbers as BigDecimal
      case VALUE_TRUE -> new BooleanDataObjectValue().withValue(true);
      case VALUE_FALSE -> new BooleanDataObjectValue().withValue(false);
      default -> throw ctxt.wrongTokenException(p, handledType(), JsonToken.START_OBJECT, null);
    };
  }
}
