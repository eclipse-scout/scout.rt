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

import java.io.Serial;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.jackson.dataobject.enumeration.EnumMapKeyDeserializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.BeanDescription.Supplier;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.KeyDeserializer;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.cfg.DeserializerFactoryConfig;
import tools.jackson.databind.deser.BeanDeserializerFactory;
import tools.jackson.databind.deser.DeserializerFactory;
import tools.jackson.databind.deser.SettableBeanProperty;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.deser.ValueInstantiator;
import tools.jackson.databind.deser.jdk.JavaUtilCollectionsDeserializers;
import tools.jackson.databind.deser.jdk.MapDeserializer;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.jsontype.TypeDeserializer;
import tools.jackson.databind.type.MapType;

/**
 * Custom {@link DeserializerFactory} that removes the special handling for {@link EnumMap}.
 * This is needed because we want to support lenient serialization of enums where we just return the key as {@link String} if it's unknown.
 *
 * @see EnumMapKeyDeserializer
 */
public class ScoutDeserializerFactory extends BeanDeserializerFactory {

  @Serial
  private static final long serialVersionUID = 1L;

  public ScoutDeserializerFactory() {
    this(new DeserializerFactoryConfig());
  }

  public ScoutDeserializerFactory(DeserializerFactoryConfig config) {
    super(config);
  }

  @Override
  public DeserializerFactory withConfig(DeserializerFactoryConfig config) {
    if (_factoryConfig == config) {
      return this;
    }
    return new ScoutDeserializerFactory(config);
  }

  @Override
  public ValueDeserializer<?> createMapDeserializer(DeserializationContext ctxt, MapType type, Supplier beanDescRef) {
    final DeserializationConfig config = ctxt.getConfig();
    final JavaType keyType = type.getKeyType();
    final JavaType contentType = type.getContentType();

    // First: is there annotation-specified deserializer for values?
    @SuppressWarnings("unchecked")
    ValueDeserializer<Object> contentDeser = (ValueDeserializer<Object>) contentType.getValueHandler();

    // Ok: need a key deserializer (null indicates 'default' here)
    KeyDeserializer keyDes = (KeyDeserializer) keyType.getValueHandler();
    // Then optional type info; either attached to type, or resolved separately:
    final TypeDeserializer contentTypeDeser = _findContentTypeDeserializer(ctxt, contentType);

    // 23-Nov-2010, tatu: Custom deserializer?
    ValueDeserializer<?> deser = _findCustomMapDeserializer(type, config, beanDescRef,
        keyDes, contentTypeDeser, contentDeser);

    if (deser == null) {
      // Value handling is identical for all, but EnumMap requires special handling for keys
      Class<?> mapClass = type.getRawClass();
      // <customized>
      //      // [databind#1853]: Map `Map<ENUM,x>` to `EnumMap<ENUM,x>`
      //      if ((mapClass == Map.class) && keyType.isEnumType()) {
      //        mapClass = EnumMap.class;
      //        type = (MapType) config.getTypeFactory().constructSpecializedType(type, mapClass);
      //        //                type = (MapType) config.getTypeFactory().constructMapType(mapClass, keyType, contentType);
      //      }
      //      if (EnumMap.class.isAssignableFrom(mapClass)) {
      //        ValueInstantiator inst;
      //
      //        // 06-Mar-2017, tatu: Should only need to check ValueInstantiator for
      //        //    custom sub-classes, see [databind#1544]
      //        if (mapClass == EnumMap.class) {
      //          inst = null;
      //        } else {
      //          inst = findValueInstantiator(ctxt, beanDescRef);
      //        }
      //        if (!keyType.isEnumImplType()) {
      //          throw new IllegalArgumentException("Cannot construct EnumMap; generic (key) type not available");
      //        }
      //        _assignCreatorViews(ctxt, beanDescRef, inst);
      //        // [databind#6252]: class-level and config-override ignorals/inclusions, same as with `MapDeserializer`
      //        JsonIgnoreProperties.Value ignorals = config.getDefaultPropertyIgnorals(Map.class,
      //            beanDescRef.getClassInfo());
      //        JsonIncludeProperties.Value inclusions = config.getDefaultPropertyInclusions(Map.class,
      //            beanDescRef.getClassInfo());
      //        deser = new EnumMapDeserializer(type, inst, null,
      //            contentDeser, contentTypeDeser, null,
      //            (ignorals == null) ? null : ignorals.findIgnoredForDeserialization(),
      //            (inclusions == null) ? null : inclusions.getIncluded());
      //      }
      // </customized>

      // Otherwise, generic handler works ok.

      /* But there is one more twist: if we are being asked to instantiate
       * an interface or abstract Map, we need to either find something
       * that implements the thing, or give up.
       *
       * Note that we do NOT try to guess based on secondary interfaces
       * here; that would probably not work correctly since casts would
       * fail later on (as the primary type is not the interface we'd
       * be implementing)
       */
      if (deser == null) {
        if (type.isInterface() || type.isAbstract()) {
          MapType implType = _mapAbstractMapType(type, config);
          if (implType != null) {
            type = (MapType) implType;
            mapClass = type.getRawClass();
            // But if so, also need to re-check creators...
            beanDescRef = ctxt.lazyIntrospectBeanDescriptionForCreation(type);
          }
          // 11-Nov-2024, tatu: Related to [databind#4783] let's not fail on
          //    abstract Maps so they can work with merge (or factory methods)
        }
        else {
          // 10-Jan-2017, tatu: `java.util.Collections` types need help:
          deser = JavaUtilCollectionsDeserializers.findForMap(ctxt, type);
          if (deser != null) {
            return deser;
          }
        }
        if (deser == null) {
          ValueInstantiator inst = findValueInstantiator(ctxt, beanDescRef);
          _assignCreatorViews(ctxt, beanDescRef, inst);
          // 01-May-2016, tatu: Which base type to use here gets tricky, since
          //   most often it ought to be `Map` or `EnumMap`, but due to abstract
          //   mapping it will more likely be concrete type like `HashMap`.
          //   So, for time being, just pass `Map.class`
          MapDeserializer md = new MapDeserializer(type, inst, keyDes, contentDeser, contentTypeDeser);
          JsonIgnoreProperties.Value ignorals = config.getDefaultPropertyIgnorals(Map.class,
              beanDescRef.getClassInfo());
          Set<String> ignored = (ignorals == null) ? null
              : ignorals.findIgnoredForDeserialization();
          md.setIgnorableProperties(ignored);
          JsonIncludeProperties.Value inclusions = config.getDefaultPropertyInclusions(Map.class,
              beanDescRef.getClassInfo());
          Set<String> included = inclusions == null ? null : inclusions.getIncluded();
          md.setIncludableProperties(included);
          deser = md;
        }
      }
    }
    if (_factoryConfig.hasDeserializerModifiers()) {
      for (ValueDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
        deser = mod.modifyMapDeserializer(config, type, beanDescRef, deser);
      }
    }
    return deser;
  }

  //  @Override
  //  public ValueDeserializer<?> createMapDeserializer(DeserializationContext ctxt, MapType type, Supplier beanDescRef) {
  //    final DeserializationConfig config = ctxt.getConfig();
  //    final JavaType keyType = type.getKeyType();
  //    final JavaType contentType = type.getContentType();
  //
  //    // First: is there annotation-specified deserializer for values?
  //    @SuppressWarnings("unchecked")
  //    ValueDeserializer<Object> contentDeser = (ValueDeserializer<Object>) contentType.getValueHandler();
  //
  //    // Ok: need a key deserializer (null indicates 'default' here)
  //    KeyDeserializer keyDes = (KeyDeserializer) keyType.getValueHandler();
  //    // Then optional type info; either attached to type, or resolved separately:
  //    final TypeDeserializer contentTypeDeser = _findContentTypeDeserializer(ctxt, contentType);
  //
  //    // 23-Nov-2010, tatu: Custom deserializer?
  //    ValueDeserializer<?> deser = _findCustomMapDeserializer(type, config, beanDescRef,
  //        keyDes, contentTypeDeser, contentDeser);
  //
  //    // <customized>
  //    //    if (deser == null) {
  //    //      // Value handling is identical for all, but EnumMap requires special handling for keys
  //    //      Class<?> mapClass = type.getRawClass();
  //    //      // [databind#1853]: Map `Map<ENUM,x>` to `EnumMap<ENUM,x>`
  //    //      if ((mapClass == Map.class) && keyType.isEnumType()) {
  //    //        mapClass = EnumMap.class;
  //    //        type = (MapType) config.getTypeFactory().constructSpecializedType(type, mapClass);
  //    //        //                type = (MapType) config.getTypeFactory().constructMapType(mapClass, keyType, contentType);
  //    //      }
  //    //      if (EnumMap.class.isAssignableFrom(mapClass)) {
  //    //        ValueInstantiator inst;
  //    //
  //    //        // 06-Mar-2017, tatu: Should only need to check ValueInstantiator for
  //    //        //    custom sub-classes, see [databind#1544]
  //    //        if (mapClass == EnumMap.class) {
  //    //          inst = null;
  //    //        } else {
  //    //          inst = findValueInstantiator(ctxt, beanDescRef);
  //    //        }
  //    //        if (!keyType.isEnumImplType()) {
  //    //          throw new IllegalArgumentException("Cannot construct EnumMap; generic (key) type not available");
  //    //        }
  //    //        _assignCreatorViews(ctxt, beanDescRef, inst);
  //    //        // [databind#6252]: class-level and config-override ignorals/inclusions, same as with `MapDeserializer`
  //    //        JsonIgnoreProperties.Value ignorals = config.getDefaultPropertyIgnorals(Map.class,
  //    //            beanDescRef.getClassInfo());
  //    //        JsonIncludeProperties.Value inclusions = config.getDefaultPropertyInclusions(Map.class,
  //    //            beanDescRef.getClassInfo());
  //    //        deser = new EnumMapDeserializer(type, inst, null,
  //    //            contentDeser, contentTypeDeser, null,
  //    //            (ignorals == null) ? null : ignorals.findIgnoredForDeserialization(),
  //    //            (inclusions == null) ? null : inclusions.getIncluded());
  //    //      }
  //    // </customized>
  //
  //    // Otherwise, generic handler works ok.
  //
  //    /* But there is one more twist: if we are being asked to instantiate
  //     * an interface or abstract Map, we need to either find something
  //     * that implements the thing, or give up.
  //     *
  //     * Note that we do NOT try to guess based on secondary interfaces
  //     * here; that would probably not work correctly since casts would
  //     * fail later on (as the primary type is not the interface we'd
  //     * be implementing)
  //     */
  //    if (deser == null) {
  //      if (type.isInterface() || type.isAbstract()) {
  //        MapType implType = _mapAbstractMapType(type, config);
  //        if (implType != null) {
  //          type = (MapType) implType;
  //          mapClass = type.getRawClass();
  //          // But if so, also need to re-check creators...
  //          beanDescRef = ctxt.lazyIntrospectBeanDescriptionForCreation(type);
  //        }
  //        // 11-Nov-2024, tatu: Related to [databind#4783] let's not fail on
  //        //    abstract Maps so they can work with merge (or factory methods)
  //      }
  //      else {
  //        // 10-Jan-2017, tatu: `java.util.Collections` types need help:
  //        deser = JavaUtilCollectionsDeserializers.findForMap(ctxt, type);
  //        if (deser != null) {
  //          return deser;
  //        }
  //      }
  //      if (deser == null) {
  //        ValueInstantiator inst = findValueInstantiator(ctxt, beanDescRef);
  //        _assignCreatorViews(ctxt, beanDescRef, inst);
  //        // 01-May-2016, tatu: Which base type to use here gets tricky, since
  //        //   most often it ought to be `Map` or `EnumMap`, but due to abstract
  //        //   mapping it will more likely be concrete type like `HashMap`.
  //        //   So, for time being, just pass `Map.class`
  //        MapDeserializer md = new MapDeserializer(type, inst, keyDes, contentDeser, contentTypeDeser);
  //        JsonIgnoreProperties.Value ignorals = config.getDefaultPropertyIgnorals(Map.class,
  //            beanDescRef.getClassInfo());
  //        Set<String> ignored = (ignorals == null) ? null
  //            : ignorals.findIgnoredForDeserialization();
  //        md.setIgnorableProperties(ignored);
  //        JsonIncludeProperties.Value inclusions = config.getDefaultPropertyInclusions(Map.class,
  //            beanDescRef.getClassInfo());
  //        Set<String> included = inclusions == null ? null : inclusions.getIncluded();
  //        md.setIncludableProperties(included);
  //        deser = md;
  //      }
  //    }
  //  }
  //    if(_factoryConfig.hasDeserializerModifiers())
  //
  //  {
  //    for (ValueDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
  //      deser = mod.modifyMapDeserializer(config, type, beanDescRef, deser);
  //    }
  //  }
  //    return deser;
  //}

  private void _assignCreatorViews(DeserializationContext ctxt,
      BeanDescription.Supplier beanDescRef, ValueInstantiator inst) {
    if ((inst != null) && inst.canCreateFromObjectWith()) {
      _assignCreatorViews(ctxt, beanDescRef, inst.getFromObjectArguments(ctxt.getConfig()));
    }
  }

  private void _assignCreatorViews(DeserializationContext ctxt,
      BeanDescription.Supplier beanDescRef, SettableBeanProperty[] creatorProps) {
    final DeserializationConfig config = ctxt.getConfig();
    final AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
    final Class<?>[] defaultViews = beanDescRef.get().findDefaultViews();
    for (SettableBeanProperty prop : creatorProps) {
      Class<?>[] views = null;
      AnnotatedMember member = prop.getMember();
      if (member != null) {
        views = intr.findViews(config, member);
      }
      if (views == null) {
        views = defaultViews;
      }
      // Unlike with POJOs, only set if we have something: avoids clearing views
      // that may have already been assigned (for example by a module)
      if (views != null) {
        prop.setViews(views);
      }
    }
  }
}
