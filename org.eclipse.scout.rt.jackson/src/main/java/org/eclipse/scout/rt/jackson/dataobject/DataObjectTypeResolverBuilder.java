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
package org.eclipse.scout.rt.jackson.dataobject;

import java.util.Collection;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.Bean;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

@Bean
public class DataObjectTypeResolverBuilder extends StdTypeResolverBuilder {

  @Override
  public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
    return useForType(baseType) ? super.buildTypeDeserializer(config, baseType, subtypes) : null;
  }

  @Override
  public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
    return useForType(baseType) ? super.buildTypeSerializer(config, baseType, subtypes) : null;
  }

  protected boolean useForType(JavaType t) {
    // do not write type information for "raw" DoEntity instances (only concrete instances, without IDoEntity marker interface)
    return !DoEntity.class.equals(t.getRawClass());
  }
}
