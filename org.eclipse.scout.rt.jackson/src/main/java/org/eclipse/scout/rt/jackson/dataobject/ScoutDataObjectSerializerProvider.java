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

import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.jackson.dataobject.enumeration.EnumDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.enumeration.EnumMapKeyDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.enumeration.EnumMapKeySerializer;
import org.eclipse.scout.rt.jackson.dataobject.enumeration.EnumSerializer;
import org.eclipse.scout.rt.jackson.dataobject.id.QualifiedIIdDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.id.QualifiedIIdMapKeyDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.id.QualifiedIIdMapKeySerializer;
import org.eclipse.scout.rt.jackson.dataobject.id.QualifiedIIdSerializer;
import org.eclipse.scout.rt.jackson.dataobject.id.UnqualifiedIIdDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.id.UnqualifiedIIdMapKeyDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.id.UnqualifiedIIdMapKeySerializer;
import org.eclipse.scout.rt.jackson.dataobject.id.UnqualifiedIIdSerializer;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Value;

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.BeanDescription.Supplier;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.KeyDeserializer;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsontype.TypeDeserializer;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.type.CollectionLikeType;
import tools.jackson.databind.type.CollectionType;
import tools.jackson.databind.type.ReferenceType;

@Order(4500)
public class ScoutDataObjectSerializerProvider implements IDataObjectSerializerProvider {

  @Override
  public ValueSerializer<?> findSerializer(ScoutDataObjectModuleContext moduleContext, JavaType type, SerializationConfig config, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides) {
    Class<?> rawClass = type.getRawClass();
    if (IDoEntity.class.isAssignableFrom(rawClass)) {
      return new DoEntitySerializer(moduleContext, type);
    }
    if (IDataObjectValue.class.isAssignableFrom(rawClass)) {
      return new DataObjectValueSerializer(type);
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
        return new UnqualifiedIIdSerializer(moduleContext, type);
      }
      else {
        return new QualifiedIIdSerializer(moduleContext);
      }
    }
    return null;
  }

  @Override
  public ValueDeserializer<?> findDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type, DeserializationConfig config, BeanDescription.Supplier beanDescRef) {
    Class<?> rawClass = type.getRawClass();
    if (IDoEntity.class.isAssignableFrom(rawClass)) {
      return new DoEntityDeserializer(moduleContext, type);
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
        return new UnqualifiedIIdDeserializer(moduleContext, idClass);
      }
      else {
        return new QualifiedIIdDeserializer(moduleContext, idClass);
      }
    }

    return null;
  }

  @Override
  public ValueSerializer<?> findKeySerializer(ScoutDataObjectModuleContext moduleContext, JavaType type, SerializationConfig config, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides) {
    Class<?> rawClass = type.getRawClass();
    if (Locale.class.isAssignableFrom(rawClass)) {
      return new LocaleMapKeySerializer();
    }
    if (IId.class.isAssignableFrom(rawClass)) {
      if (type.isConcrete()) {
        return new UnqualifiedIIdMapKeySerializer(moduleContext);
      }
      else {
        return new QualifiedIIdMapKeySerializer(moduleContext);
      }
    }
    if (IEnum.class.isAssignableFrom(rawClass)) {
      return new EnumMapKeySerializer();
    }

    return null;
  }

  @Override
  public KeyDeserializer findKeyDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type, DeserializationConfig config, BeanDescription.Supplier beanDescRef) {
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
  public ValueSerializer<?> findReferenceSerializer(ScoutDataObjectModuleContext moduleContext, ReferenceType refType, SerializationConfig config, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides, TypeSerializer contentTypeSerializer,
      ValueSerializer<Object> contentValueSerializer) {
    if (DoValue.class.isAssignableFrom(refType.getRawClass())) {
      boolean staticTyping = (contentTypeSerializer == null) && config.isEnabled(MapperFeature.USE_STATIC_TYPING);
      return new DoValueSerializer(refType, staticTyping, contentTypeSerializer, contentValueSerializer);
    }

    return null;
  }

  @Override
  public ValueDeserializer<?> findReferenceDeserializer(ScoutDataObjectModuleContext moduleContext, ReferenceType refType, DeserializationConfig config, BeanDescription.Supplier beanDescRef, TypeDeserializer contentTypeDeserializer,
      ValueDeserializer<?> contentDeserializer) {
    if (refType.hasRawClass(DoValue.class)) {
      return new DoValueDeserializer(refType, null, contentTypeDeserializer, contentDeserializer);
    }

    return null;
  }

  @Override
  public ValueSerializer<?> findCollectionSerializer(ScoutDataObjectModuleContext moduleContext, CollectionType type, SerializationConfig config, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides, TypeSerializer elementTypeSerializer, ValueSerializer<Object> elementValueSerializer) {
    if (Collection.class.isAssignableFrom(type.getRawClass())) {
      return new DoCollectionSerializer<>(moduleContext, type);
    }
    return null;
  }

  @Override
  public ValueSerializer<?> findCollectionLikeSerializer(ScoutDataObjectModuleContext moduleContext, CollectionLikeType type, SerializationConfig config, Supplier beanDescRef, Value formatOverrides, TypeSerializer elementTypeSerializer, ValueSerializer<Object> elementValueSerializer) {
    if (ObjectUtility.isOneOf(type.getRawClass(), DoList.class, DoSet.class, DoCollection.class)) {
      return new DoCollectionSerializer<>(moduleContext, type);
    }
    return null;
  }

  @Override
  public ValueDeserializer<?> findCollectionLikeDeserializer(ScoutDataObjectModuleContext moduleContext, CollectionLikeType type, DeserializationConfig config, Supplier beanDescRef, TypeDeserializer elementTypeDeserializer, ValueDeserializer<?> elementDeserializer) {
    if (DoList.class.isAssignableFrom(type.getRawClass())) {
      return new DoCollectionDeserializer<>(type, DoList::new);
    }
    else if (DoSet.class.isAssignableFrom(type.getRawClass())) {
      return new DoCollectionDeserializer<>(type, DoSet::new);
    }
    // using default collection deserializer, no handling as for serialization required in deserialization
    else if (DoCollection.class.isAssignableFrom(type.getRawClass())) {
      return new DoCollectionDeserializer<>(type, DoCollection::new);
    }
    return null;
  }

  @Override
  public ValueSerializer<?> findEnumSerializer(ScoutDataObjectModuleContext moduleContext, JavaType type, SerializationConfig config, Supplier beanDescRef, Value formatOverrides) {
    if (IEnum.class.isAssignableFrom(type.getRawClass())) {
      return new EnumSerializer(type);
    }
    return null;
  }

  @Override
  public ValueDeserializer<?> findEnumDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type, DeserializationConfig config, BeanDescription.Supplier beanDescRef) {
    if (IEnum.class.isAssignableFrom(type.getRawClass())) {
      return new EnumDeserializer(type.getRawClass().asSubclass(IEnum.class));
    }
    return null;
  }
}
