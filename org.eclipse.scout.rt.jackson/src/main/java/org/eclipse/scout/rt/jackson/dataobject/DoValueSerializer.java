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

import org.eclipse.scout.rt.dataobject.DoValue;

import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.std.ReferenceTypeSerializer;
import tools.jackson.databind.type.ReferenceType;
import tools.jackson.databind.util.NameTransformer;

/**
 * Serializer for {@link DoValue} objects wrapping a value object.
 */
public class DoValueSerializer extends ReferenceTypeSerializer<DoValue<?>> {

  public DoValueSerializer(ReferenceType fullType, boolean staticTyping, TypeSerializer vts, ValueSerializer<Object> ser) {
    super(fullType, staticTyping, vts, ser);
  }

  public DoValueSerializer(DoValueSerializer base, BeanProperty property, TypeSerializer vts, ValueSerializer<?> valueSer, NameTransformer unwrapper, Object suppressableValue, boolean suppressNulls) {
    super(base, property, vts, valueSer, unwrapper, suppressableValue, suppressNulls);
  }

  @Override
  protected ReferenceTypeSerializer<DoValue<?>> withResolved(BeanProperty prop, TypeSerializer vts, ValueSerializer<?> valueSer, NameTransformer unwrapper) {
    if ((_property == prop) && (_valueTypeSerializer == vts) && (_valueSerializer == valueSer) && (_unwrapper == unwrapper)) {
      return this;
    }
    return new DoValueSerializer(this, prop, vts, valueSer, unwrapper, _suppressableValue, _suppressNulls);
  }

  @Override
  public ReferenceTypeSerializer<DoValue<?>> withContentInclusion(Object suppressableValue, boolean suppressNulls) {
    return new DoValueSerializer(this, _property, _valueTypeSerializer, _valueSerializer, _unwrapper, suppressableValue, suppressNulls);
  }

  @Override
  protected boolean _isValuePresent(DoValue<?> value) {
    return value.exists();
  }

  @Override
  protected Object _getReferenced(DoValue<?> value) {
    return value.get();
  }

  @Override
  protected Object _getReferencedIfPresent(DoValue<?> value) {
    return value.exists() ? value.get() : null;
  }
}
