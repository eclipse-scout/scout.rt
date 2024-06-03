/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
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
import java.math.BigDecimal;
import java.util.Collection;

import org.eclipse.scout.rt.dataobject.AbstractDoCollection;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoMapEntity;
import org.eclipse.scout.rt.dataobject.DoNode;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoCollection;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserSequence;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/**
 * Deserializer for {@link DoEntity} and all sub-classes.
 */
public class DoEntityDeserializer extends StdDeserializer<IDoEntity> {
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(DoEntityDeserializer.class);

  protected final ScoutDataObjectModuleContext m_moduleContext;
  protected final JavaType m_handledType;
  protected final Class<? extends IDoEntity> m_handledClass;
  protected final IDoEntityDeserializerTypeStrategy m_doEntityDeserializerTypeStrategy;

  public DoEntityDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type) {
    super(type);
    m_moduleContext = moduleContext;
    m_handledType = type;
    m_handledClass = type.getRawClass().asSubclass(IDoEntity.class);
    m_doEntityDeserializerTypeStrategy = initDoEntityTypeStrategy(moduleContext);
  }

  protected IDoEntityDeserializerTypeStrategy initDoEntityTypeStrategy(ScoutDataObjectModuleContext moduleContext) {
    if (moduleContext.isIgnoreTypeAttribute()) {
      return BEANS.get(RawDoEntityDeserializerTypeStrategy.class);
    }
    return BEANS.get(DefaultDoEntityDeserializerTypeStrategy.class);
  }

  @Override
  public IDoEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return deserializeDoEntity(p, ctxt, null);
  }

  @Override
  public IDoEntity deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
    return deserializeDoEntity(p, ctxt, typeDeserializer);
  }

  protected IDoEntity deserializeDoEntity(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
    switch (p.nextToken()) {
      case FIELD_NAME:
        return deserializeDoEntityAttributes(p, ctxt);
      case END_OBJECT:
        // empty object without attributes consists only of START_OBJECT and END_OBJECT token, return empty entity object
        return newObject(ctxt, m_handledClass);
      default:
        throw ctxt.wrongTokenException(p, m_handledType, JsonToken.FIELD_NAME, null);
    }
  }

  @SuppressWarnings({"resource", "squid:S2095"})
  protected IDoEntity deserializeDoEntityAttributes(JsonParser p, DeserializationContext ctxt) throws IOException {
    // search for type property within attributes of DoEntity, cache other fields within token buffer
    TokenBuffer tb = null;
    for (JsonToken t = p.currentToken(); t == JsonToken.FIELD_NAME; t = p.nextToken()) {
      String attributeName = p.getCurrentName();
      p.nextToken(); // let current token point to the value

      // check if found the type property
      if (m_moduleContext.getTypeAttributeName().equals(attributeName)) {
        String entityType = p.getText();
        IDoEntity entity = resolveEntityType(ctxt, entityType);
        p.setCurrentValue(entity); // set current entity object as current value on parser (used for correct typing of field read after finding _type attribute)

        // put back the cached fields of token buffer to parser, if any fields were cached while searching type property
        if (tb != null) {
          p.clearCurrentToken();
          JsonParser tbParser = tb.asParser(p);
          tbParser.setCurrentValue(entity); // set current entity object as current value on parser created out of cached token buffer (used for correct typing of field read into token buffer before finding _type attribute)
          p = JsonParserSequence.createFlattened(false, tbParser, p);
        }
        p.nextToken(); // skip type field value
        return deserializeDoEntityAttributes(p, ctxt, entity);
      }

      // lazy create token buffer to cache other fields
      if (tb == null) {
        tb = new TokenBuffer(p, ctxt);
      }

      // write current field name and value to token buffer for later parsing
      tb.writeFieldName(attributeName);
      tb.copyCurrentStructure(p);
    }

    // if any fields where cached, finish token buffer and move parser back to cached token buffer fields
    if (tb != null) {
      tb.writeEndObject();
      p = tb.asParser(p);
      // initialize parser pointing to first field token
      p.nextToken();
    }

    // no type information found within available attributes, resolve default entity type (null = use default entity type)
    IDoEntity entity = resolveEntityType(ctxt, null);
    p.setCurrentValue(entity); // set current value after new parser instance was created out of token buffer

    return deserializeDoEntityAttributes(p, ctxt, entity);
  }

  protected IDoEntity deserializeDoEntityAttributes(JsonParser p, DeserializationContext ctxt, IDoEntity entity) throws IOException {
    // read and deserialize all fields of entity
    for (JsonToken t = p.currentToken(); t == JsonToken.FIELD_NAME; t = p.nextToken()) {
      String attributeName = p.getCurrentName();
      p.nextToken(); // let current token point to the value
      AttributeType attributeType = findResolvedAttributeType(entity, attributeName, p.currentToken());
      if (attributeType.isDoCollection()) {
        DoNode<?> nodeValue = readAttributeValue(p, attributeType, attributeName);

        // check if reading the 'contributions' property
        if (m_moduleContext.getContributionsAttributeName().equals(attributeName)) {
          //noinspection unchecked
          m_doEntityDeserializerTypeStrategy.putContributions(entity, attributeName, ((AbstractDoCollection<?, Collection<?>>) nodeValue).get());
        }
        else {
          entity.putNode(attributeName, nodeValue);
        }
      }
      else {
        Object value = readAttributeValue(p, attributeType, attributeName);

        if (value instanceof IDoCollection) {
          // special case: Java collections referenced by a DoValue are handled as DoList, so that nested elements are
          //               deserialized into data object structures (i.e. Do-classes), rather than Maps, such as Jackson's
          //               default collection deserializer would use.
          value = ((IDoCollection) value).get();
        }

        // check if reading the 'type version' property
        if (m_moduleContext.getTypeVersionAttributeName().equals(attributeName)) {
          deserializeDoEntityVersionAttribute(entity, attributeName, value);
        }
        else {
          entity.put(attributeName, value);
        }
      }
    }
    return entity;
  }

  protected <T> T readAttributeValue(JsonParser p, AttributeType attributeType, String attributeName) throws IOException {
    try {
      return p.getCodec().readValue(p, attributeType.getJavaType());
    }
    catch (InvalidFormatException e) {
      // capture exception containing the deserialized value to throw a specific exception message with attribute name and entity class
      String msg = MessageFormatter.arrayFormat("Failed to deserialize attribute '{}' of entity {}, value was {}, message={}", new Object[]{attributeName, handledType().getName(), e.getValue(), e.getMessage()}).getMessage();
      InvalidFormatException ife = InvalidFormatException.from(p, msg, e.getValue(), e.getTargetType());
      ife.addSuppressed(e);
      throw ife;
    }
    catch (IOException e) {
      // capture generic exception to add at least the attribute name and entity class to the exception message
      String msg = MessageFormatter.arrayFormat("Failed to deserialize attribute '{}' of entity {}, message={}", new Object[]{attributeName, handledType().getName(), e.getMessage()}).getMessage();
      throw JsonMappingException.from(p, msg, e);
    }
  }

  protected void deserializeDoEntityVersionAttribute(IDoEntity entity, String attributeName, Object version) {
    String dataObjectTypeVersion = m_doEntityDeserializerTypeStrategy.resolveTypeVersion(entity.getClass());
    if (dataObjectTypeVersion != null) {
      // entity class type has a type version, check deserialized type version against entity class type version
      if (!dataObjectTypeVersion.equals(version)) {
        LOG.warn("Found version mismatch while deserializing DoEntity {}. Data object version (in class file) '{}', deserialized data version '{}'",
            entity.getClass().getName(), dataObjectTypeVersion, version);
      }
    }
    else {
      // class type does not have a type version, add deserialized version as additional attribute (raw version support)
      entity.put(attributeName, version);
    }
  }

  protected IDoEntity resolveEntityType(DeserializationContext ctxt, String entityType) throws IOException {
    if (entityType != null) {
      // try to lookup DoEntity with specified entityType
      Class<? extends IDoEntity> clazz = m_doEntityDeserializerTypeStrategy.resolveTypeName(entityType);
      if (clazz != null) {
        // (1) Class could be resolved by given entityType, validate that resolved class is assignable from class handled by this deserializer instance
        if (!m_handledClass.isAssignableFrom(clazz)) {
          throw JsonMappingException.from(ctxt, "Class resolved by parsed entity type is not assignable from class expected by deserializer. ["
              + "entityType=" + entityType + " resolvedClass=" + clazz.getName() + " handledClassByDeserializer=" + m_handledClass + "]");
        }
        return newObject(ctxt, clazz);
      }
      else {
        // (2) Class could be not resolved by given entityType, validate that handled class (e.g. class expected to be created by this deserializer instance) is of raw type
        if (!ObjectUtility.isOneOf(m_handledClass, DoEntity.class, IDoEntity.class, IDataObject.class)) {
          throw JsonMappingException.from(ctxt, "Could not resolve a class by parsed entity type and deserializer expect a concrete class to be created. ["
              + "entityType=" + entityType + " handledClassByDeserializer=" + m_handledClass + "]");
        }
        // Use generic DoEntity instance with a type attribute to preserve the type information even if correct DoEntity class could not be resolved
        DoEntity entity = newObject(ctxt, DoEntity.class);
        entity.put(m_moduleContext.getTypeAttributeName(), entityType);
        return entity;
      }
    }
    // Fallback to handled type of deserializer
    return newObject(ctxt, m_handledClass);
  }

  protected AttributeType findResolvedAttributeType(IDoEntity entityInstance, String attributeName, JsonToken currentToken) {
    return m_doEntityDeserializerTypeStrategy.resolveAttributeType(entityInstance.getClass(), attributeName, currentToken)
        .orElseGet(() -> findResolvedFallbackAttributeType(entityInstance, attributeName, currentToken));
  }

  protected AttributeType findResolvedFallbackAttributeType(IDoEntity entityInstance, String attributeName, JsonToken currentToken) {
    if (DoMapEntity.class.isAssignableFrom(m_handledClass)) {
      // DoMapEntity<T> structure is deserialized as typed Map<String, T>
      return AttributeType.ofDoValue(findResolvedDoMapEntityType());
    }
    if (currentToken == JsonToken.START_OBJECT) {
      // fallback to default handling, if no attribute definition could be found
      return AttributeType.ofDoValue(TypeFactory.defaultInstance().constructType(DoEntity.class));
    }
    if (currentToken == JsonToken.START_ARRAY) {
      // array-like JSON structure is deserialized as raw DoList (using DoList as generic structure instead of DoSet or DoCollection)
      return AttributeType.ofDoCollection(TypeFactory.defaultInstance().constructType(DoList.class));
    }
    if (currentToken == JsonToken.VALUE_NUMBER_FLOAT) {
      // deserialize floating point numbers as BigDecimal
      return AttributeType.ofDoValue(TypeFactory.defaultInstance().constructType(BigDecimal.class));
    }
    // JSON scalar values are deserialized as raw object using default jackson typing
    return AttributeType.ofDoValue(TypeFactory.unknownType());
  }

  /**
   * Lookup generic type parameter of DoMapEntity super class
   */
  protected JavaType findResolvedDoMapEntityType() {
    JavaType type = m_handledType;
    while (type.getRawClass() != DoMapEntity.class) {
      if (type.getRawClass() == Object.class) {
        // Fallback: object-like JSON structure is deserialized as raw DoEntity
        return TypeFactory.defaultInstance().constructType(DoEntity.class);
      }
      type = type.getSuperClass();
    }
    // Use type parameter of DoMap<T> as attribute type
    return type.getBindings().getBoundType(0);
  }

  protected <T extends IDoEntity> T newObject(DeserializationContext ctxt, Class<T> entityType) throws IOException {
    if (entityType == IDoEntity.class) {
      // fallback to default DoEntity implementation, if handled entity type is IDoEntity (e.g. class instance is unspecified)
      return entityType.cast(BEANS.get(DoEntity.class));
    }
    else if (BEANS.getBeanManager().isBean(entityType)) {
      return BEANS.get(entityType);
    }
    throw JsonMappingException.from(ctxt, "Could not instantiate class, " + (entityType == null ? null : entityType.getName()) + " is not a Scout bean");
  }
}
