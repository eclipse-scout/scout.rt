/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject;

import org.eclipse.scout.rt.dataobject.DoValue;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ReferenceTypeSerializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.util.NameTransformer;

/**
 * Serializer for {@link DoValue} objects wrapping a value object.
 */
public class DoValueSerializer extends ReferenceTypeSerializer<DoValue<?>> {
  private static final long serialVersionUID = 1L;

  public DoValueSerializer(ReferenceType fullType, boolean staticTyping, TypeSerializer vts, JsonSerializer<Object> ser) {
    super(fullType, staticTyping, vts, ser);
  }

  public DoValueSerializer(DoValueSerializer base, BeanProperty property, TypeSerializer vts, JsonSerializer<?> valueSer, NameTransformer unwrapper, Object suppressableValue, boolean suppressNulls) {
    super(base, property, vts, valueSer, unwrapper, suppressableValue, suppressNulls);
  }

  @Override
  protected ReferenceTypeSerializer<DoValue<?>> withResolved(BeanProperty prop, TypeSerializer vts, JsonSerializer<?> valueSer, NameTransformer unwrapper) {
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
