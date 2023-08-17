/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import org.eclipse.scout.rt.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.IDoCollection;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer for {@link IDoCollection} subclasses ({@link DoList}, {@link DoSet} and {@link DoCollection}) and
 * {@link Collection}.
 */
public class DoCollectionSerializer<COLLECTION extends Iterable<?>> extends StdSerializer<COLLECTION> {
  private static final long serialVersionUID = 1L;

  protected final LazyValue<DataObjectInventory> m_dataObjectInventory = new LazyValue<>(DataObjectInventory.class);

  protected final ScoutDataObjectModuleContext m_context;

  public DoCollectionSerializer(ScoutDataObjectModuleContext context, JavaType type) {
    super(type);
    m_context = context;
  }

  @Override
  public void serialize(COLLECTION value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    serializeList(value, gen, provider);
  }

  @Override
  public void serializeWithType(COLLECTION value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
    serializeList(value, gen, serializers);
  }

  protected void serializeList(Iterable<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    JavaType listElementType = null;
    JsonSerializer<Object> serializer = null;
    JsonStreamContext ctx = gen.getOutputContext();
    if (ctx.getCurrentValue() instanceof IDoEntity) {
      String attributeName = ctx.getCurrentName();
      Optional<AttributeType> typeOpt = getAttributeType(((IDoEntity) ctx.getCurrentValue()).getClass(), attributeName);
      if (typeOpt.isPresent()) {
        // Either DoCollection/DoList/DoSet or a Collection (e.g. Collection<X>)
        listElementType = typeOpt.get().getJavaType().getBindings().getBoundType(0);

        // Check for != Object is required because findTypedValueSerializer would otherwise return UnknownSerializer.
        // By not setting a serializer here, JsonGenerator#writeObject will be called further below, which will result in a value-based serialization.
        // listElementType will be null for DoValue<IDataObject>
        if (listElementType != null && listElementType.getRawClass() != Object.class) {
          serializer = provider.findTypedValueSerializer(listElementType, true, null);
        }
      }
    }

    // serialize AbstractDoCollection as array using default jackson serializer (includes types if necessary according to actual chosen serializer for each object type)
    gen.writeStartArray();
    gen.setCurrentValue(value);
    for (Object item : value) {
      if (serializer == null || item == null || (m_context.isLenientMode() && !listElementType.isTypeOrSuperTypeOf(item.getClass()))) {
        // Use value-based serialization either:
        // - if no type information on data object entity class is available for given attribute (serializer is null)
        // - if item value is null (JsonSerializer#serializer must not be called will a null value)
        // - if lenient mode and declared list element type is not equals/not a super type of the given item value
        gen.writeObject(item);
      }
      else {
        serializer.serialize(item, gen, provider);
      }
    }
    gen.writeEndArray();
  }

  protected Optional<AttributeType> getAttributeType(Class<? extends IDoEntity> entityClass, String attributeName) {
    return m_dataObjectInventory.get().getAttributeDescription(entityClass, attributeName)
        .map(a -> TypeFactoryUtility.toAttributeType(a.getType()))
        .filter(AttributeType::isKnown); // filter completely unknown types, forcing to use the default behavior for unknown types
  }

  @Override
  public boolean isEmpty(SerializerProvider provider, COLLECTION value) {
    return !value.iterator().hasNext();
  }
}
