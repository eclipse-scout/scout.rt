/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.scout.rt.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

@Bean
public class DataObjectTypeResolverBuilder extends StdTypeResolverBuilder {

  private final LazyValue<DataObjectInventory> m_dataObjectInventory = new LazyValue<>(DataObjectInventory.class);

  @Override
  public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
    return useForType(baseType) ? super.buildTypeDeserializer(config, baseType, subtypes) : null;
  }

  @Override
  public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
    return useForType(baseType) ? new DataObjectAsPropertyTypeSerializer(_customIdResolver, getTypeProperty()) : null;
  }

  protected boolean useForType(JavaType t) {
    // IDoEntity and sub-interfaces force to write a type name
    if (t.getRawClass().isInterface()) {
      return true;
    }
    // write type name if class is annotated with a type name
    return m_dataObjectInventory.get().toTypeName(t.getRawClass()) != null;
  }

  /**
   * Custom {@link AsPropertyTypeSerializer} implementation used to add type information to serialized data object, but
   * only if the class of the runtime value is annotated with type information (see {@link TypeName}).
   */
  protected static class DataObjectAsPropertyTypeSerializer extends AsPropertyTypeSerializer {

    public DataObjectAsPropertyTypeSerializer(TypeIdResolver idRes, String propName) {
      super(idRes, null, propName);
    }

    @Override
    public WritableTypeId writeTypePrefix(JsonGenerator g, WritableTypeId idMetadata) throws IOException {
      _generateTypeId(idMetadata);
      if (idMetadata.id != null) {
        return g.writeTypePrefix(idMetadata);
      }
      else {
        // Skip writing type information if no type id could be generated for given value (e.g. class structure
        // indicated a type name, but runtime class of value to be serialized is DoEntity which does not provide any
        // type information by class). Note that the raw DoEntity which is serialized could contain a raw type
        // information as ordinary attribute (which is preserved as is).
        g.writeStartObject(idMetadata.forValue);
        return idMetadata;
      }
    }
  }
}
