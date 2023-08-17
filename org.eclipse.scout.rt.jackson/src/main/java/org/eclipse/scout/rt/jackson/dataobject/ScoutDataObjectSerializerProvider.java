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

import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.TypedId;
import org.eclipse.scout.rt.jackson.dataobject.enumeration.EnumDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.enumeration.EnumMapKeyDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.enumeration.EnumMapKeySerializer;
import org.eclipse.scout.rt.jackson.dataobject.enumeration.EnumSerializer;
import org.eclipse.scout.rt.jackson.dataobject.id.QualifiedIIdDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.id.QualifiedIIdMapKeyDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.id.QualifiedIIdMapKeySerializer;
import org.eclipse.scout.rt.jackson.dataobject.id.QualifiedIIdSerializer;
import org.eclipse.scout.rt.jackson.dataobject.id.TypedIdDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.id.TypedIdSerializer;
import org.eclipse.scout.rt.jackson.dataobject.id.UnqualifiedIIdDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.id.UnqualifiedIIdMapKeyDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.id.UnqualifiedIIdMapKeySerializer;
import org.eclipse.scout.rt.jackson.dataobject.id.UnqualifiedIIdSerializer;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.ReferenceType;

public class ScoutDataObjectSerializerProvider implements IDataObjectSerializerProvider {

  @Override
  public JsonSerializer<?> findSerializer(ScoutDataObjectModuleContext moduleContext, JavaType type, SerializationConfig config, BeanDescription beanDesc) {
    Class<?> rawClass = type.getRawClass();
    if (IDoEntity.class.isAssignableFrom(rawClass)) {
      return new DoEntitySerializer(moduleContext, type);
    }
    else if (ObjectUtility.isOneOf(rawClass, DoList.class, DoSet.class, DoCollection.class)) {
      return new DoCollectionSerializer<>(moduleContext, type);
    }
    else if (Date.class.isAssignableFrom(rawClass)) {
      return new DoDateSerializer();
    }
    else if (Locale.class.isAssignableFrom(rawClass)) {
      return new DoLocaleSerializer();
    }
    else if (BinaryResource.class.isAssignableFrom(rawClass)) {
      return new DoBinaryResourceSerializer();
    }
    else if (IId.class.isAssignableFrom(rawClass)) {
      if (type.isConcrete()) {
        return new UnqualifiedIIdSerializer(type);
      }
      else {
        return new QualifiedIIdSerializer();
      }
    }
    else if (TypedId.class.isAssignableFrom(rawClass)) {
      return new TypedIdSerializer();
    }
    else if (IEnum.class.isAssignableFrom(rawClass)) {
      return new EnumSerializer(type);
    }
    return null;
  }

  @Override
  public JsonDeserializer<?> findDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
    Class<?> rawClass = type.getRawClass();
    if (IDoEntity.class.isAssignableFrom(rawClass)) {
      return new DoEntityDeserializer(moduleContext, type);
    }
    else if (DoList.class.isAssignableFrom(rawClass)) {
      return new DoCollectionDeserializer<>(type, DoList::new);
    }
    else if (DoSet.class.isAssignableFrom(rawClass)) {
      return new DoCollectionDeserializer<>(type, DoSet::new);
    }
    // using default collection deserializer, no handling as for serialization required in deserialization
    else if (DoCollection.class.isAssignableFrom(rawClass)) {
      return new DoCollectionDeserializer<>(type, DoCollection::new);
    }
    else if (Date.class.isAssignableFrom(rawClass)) {
      return new DoDateDeserializer();
    }
    else if (IDataObject.class.isAssignableFrom(rawClass)) {
      return new DataObjectDeserializer(type.getRawClass());
    }
    else if (Locale.class.isAssignableFrom(rawClass)) {
      return new DoLocaleDeserializer();
    }
    else if (Currency.class.isAssignableFrom(rawClass)) {
      // only deserializer, no serializer required
      return new DoCurrencyDeserializer();
    }
    else if (BinaryResource.class.isAssignableFrom(rawClass)) {
      return new DoBinaryResourceDeserializer();
    }
    else if (IId.class.isAssignableFrom(rawClass)) {
      Class<? extends IId> idClass = rawClass.asSubclass(IId.class);
      if (type.isConcrete()) {
        return new UnqualifiedIIdDeserializer(idClass);
      }
      else {
        return new QualifiedIIdDeserializer(idClass);
      }
    }
    else if (TypedId.class.isAssignableFrom(rawClass)) {
      return new TypedIdDeserializer();
    }

    return null;
  }

  @Override
  public JsonSerializer<?> findKeySerializer(ScoutDataObjectModuleContext moduleContext, JavaType type, SerializationConfig config, BeanDescription beanDesc) {
    Class<?> rawClass = type.getRawClass();
    if (Locale.class.isAssignableFrom(rawClass)) {
      return new LocaleMapKeySerializer();
    }
    if (IId.class.isAssignableFrom(rawClass)) {
      if (type.isConcrete()) {
        return new UnqualifiedIIdMapKeySerializer();
      }
      else {
        return new QualifiedIIdMapKeySerializer();
      }
    }
    if (IEnum.class.isAssignableFrom(rawClass)) {
      return new EnumMapKeySerializer();
    }

    return null;
  }

  @Override
  public KeyDeserializer findKeyDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
    Class<?> rawClass = type.getRawClass();
    if (Locale.class.isAssignableFrom(rawClass)) {
      return new LocaleMapKeyDeserializer();
    }
    if (Currency.class.isAssignableFrom(rawClass)) {
      // only key deserializer, no key serializer required
      return new CurrencyMapKeyDeserializer();
    }
    if (IId.class.isAssignableFrom(rawClass)) {
      Class<? extends IId> idClass = rawClass.asSubclass(IId.class);
      if (type.isConcrete()) {
        return new UnqualifiedIIdMapKeyDeserializer(moduleContext, idClass);
      }
      else {
        return new QualifiedIIdMapKeyDeserializer(moduleContext, idClass);
      }
    }
    else if (IEnum.class.isAssignableFrom(rawClass)) {
      Class<? extends IEnum> enumClass = rawClass.asSubclass(IEnum.class);
      return new EnumMapKeyDeserializer(moduleContext, enumClass);
    }

    return null;
  }

  @Override
  public JsonSerializer<?> findReferenceSerializer(ScoutDataObjectModuleContext moduleContext, ReferenceType refType, SerializationConfig config, BeanDescription beanDesc, TypeSerializer contentTypeSerializer,
      JsonSerializer<Object> contentValueSerializer) {
    if (DoValue.class.isAssignableFrom(refType.getRawClass())) {
      boolean staticTyping = (contentTypeSerializer == null) && config.isEnabled(MapperFeature.USE_STATIC_TYPING);
      return new DoValueSerializer(refType, staticTyping, contentTypeSerializer, contentValueSerializer);
    }

    return null;
  }

  @Override
  public JsonDeserializer<?> findReferenceDeserializer(ScoutDataObjectModuleContext moduleContext, ReferenceType refType, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer contentTypeDeserializer,
      JsonDeserializer<?> contentDeserializer) {
    if (refType.hasRawClass(DoValue.class)) {
      return new DoValueDeserializer(refType, null, contentTypeDeserializer, contentDeserializer);
    }

    return null;
  }

  @Override
  public JsonSerializer<?> findCollectionSerializer(ScoutDataObjectModuleContext moduleContext, CollectionType type, SerializationConfig config, BeanDescription beanDesc, TypeSerializer elementTypeSerializer,
      JsonSerializer<Object> elementValueSerializer) {
    if (Collection.class.isAssignableFrom(type.getRawClass())) {
      return new DoCollectionSerializer<>(moduleContext, type);
    }
    return null;
  }

  @Override
  public JsonDeserializer<?> findEnumDeserializer(ScoutDataObjectModuleContext moduleContext, Class<?> type, DeserializationConfig config, BeanDescription beanDesc) {
    if (IEnum.class.isAssignableFrom(type)) {
      return new EnumDeserializer(type.asSubclass(IEnum.class));
    }
    return null;
  }
}
