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

import org.eclipse.scout.rt.platform.dataobject.DoValue;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.ReferenceTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/**
 * Deserializer for {@link DoValue} objects unwrapping the contained value object.
 */
public class DoValueDeserializer extends ReferenceTypeDeserializer<DoValue<?>> {
  private static final long serialVersionUID = 1L;

  public DoValueDeserializer(JavaType fullType, ValueInstantiator inst, TypeDeserializer typeDeser, JsonDeserializer<?> deser) {
    super(fullType, inst, typeDeser, deser);
  }

  @Override
  public DoValueDeserializer withResolved(TypeDeserializer typeDeser, JsonDeserializer<?> valueDeser) {
    return new DoValueDeserializer(_fullType, _valueInstantiator,
        typeDeser, valueDeser);
  }

  @Override
  public DoValue<?> getNullValue(DeserializationContext ctxt) {
    return DoValue.of(null);
  }

  @Override
  public DoValue<?> referenceValue(Object contents) {
    return DoValue.of(contents);
  }

  @Override
  public Object getReferenced(DoValue<?> reference) {
    return reference.get();
  }

  @Override
  public DoValue<?> updateReference(DoValue<?> reference, Object contents) {
    return referenceValue(contents);
  }
}
