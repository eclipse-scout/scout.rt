package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoMapEntity;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.util.JsonParserSequence;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/**
 * Deserializer for {@link DoEntity} and all sub-classes.
 */
public class DoEntityDeserializer extends StdDeserializer<IDoEntity> {
  private static final long serialVersionUID = 1L;

  protected final LazyValue<DataObjectInventory> m_dataObjectInventory = new LazyValue<>(DataObjectInventory.class);

  protected final ScoutDataObjectModuleContext m_moduleContext;
  protected final JavaType m_handledType;
  protected final Class<? extends IDoEntity> m_handledClass;

  public DoEntityDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type) {
    super(type);
    m_moduleContext = moduleContext;
    m_handledType = type;
    m_handledClass = type.getRawClass().asSubclass(IDoEntity.class);
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
        p.setCurrentValue(entity); // set current entity object as current value on parser before parser is merged with token buffer holding cached fields

        // put back the cached fields of token buffer to parser, if any fields were cached while searching type property
        if (tb != null) {
          p.clearCurrentToken();
          p = JsonParserSequence.createFlattened(false, tb.asParser(p), p);
        }
        p.nextToken(); // skip type field value
        return derializeDoEntityAttributes(p, ctxt, entity);
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

    return derializeDoEntityAttributes(p, ctxt, entity);
  }

  protected IDoEntity derializeDoEntityAttributes(JsonParser p, DeserializationContext ctxt, IDoEntity entity) throws IOException {
    // read and deserialize all fields of entity
    for (JsonToken t = p.currentToken(); t == JsonToken.FIELD_NAME; t = p.nextToken()) {
      String attributeName = p.getCurrentName();
      p.nextToken(); // let current token point to the value
      boolean isArray = p.getCurrentToken() == JsonToken.START_ARRAY;
      boolean isObject = p.getCurrentToken() == JsonToken.START_OBJECT;
      ResolvedType attributeType = findResolvedAttributeType(entity, attributeName, isObject, isArray);
      if (attributeType.hasRawClass(DoList.class)) {
        DoList<?> listValue = p.getCodec().readValue(p, attributeType);
        entity.putNode(attributeName, listValue);
      }
      else {
        Object value = p.getCodec().readValue(p, attributeType);
        entity.put(attributeName, value);
      }
    }
    return entity;
  }

  protected IDoEntity resolveEntityType(DeserializationContext ctxt, String entityType) throws IOException {
    if (entityType != null) {
      // try to lookup DoEntity with specified entityType
      Class<? extends IDoEntity> clazz = m_dataObjectInventory.get().fromTypeName(entityType);
      if (clazz != null) {
        return newObject(ctxt, clazz);
      }
      else {
        // use generic DoEntity instance with a type attribute to preserve the type information even if correct DoEntity class could not be resolved
        DoEntity entity = newObject(ctxt, DoEntity.class);
        entity.put(m_moduleContext.getTypeAttributeName(), entityType);
        return entity;
      }
    }
    // fallback to handled type of deserializer
    return newObject(ctxt, m_handledClass);
  }

  protected JavaType findResolvedAttributeType(IDoEntity entityInstance, String attributeName, boolean isObject, boolean isArray) {
    return m_dataObjectInventory.get().getAttributeDescription(entityInstance.getClass(), attributeName)
        .map(a -> TypeFactoryUtility.toJavaType(a.getType()))
        .filter(type -> type.getRawClass() != Object.class) // filter completely unknown types, forcing to use the default behavior for unknown types
        .orElseGet(() -> findResolvedFallbackAttributeType(isObject, isArray));
  }

  protected JavaType findResolvedFallbackAttributeType(boolean isObject, boolean isArray) {
    // fallback to default handling, if no attribute definition could be found
    if (isObject) {
      // DoMapEntity<T> structure is deserialized as typed Map<String, T>
      if (DoMapEntity.class.isAssignableFrom(m_handledClass)) {
        return findResolvedDoMapEntityType();
      }
      else {
        // object-like JSON structure is deserialized as raw DoEntity
        return TypeFactory.defaultInstance().constructType(DoEntity.class);
      }
    }
    else if (isArray) {
      // array-like JSON structure is deserialized as raw DoList
      return TypeFactory.defaultInstance().constructType(DoList.class);
    }
    else {
      // JSON scalar values are deserialized as raw object using default jackson typing
      return TypeFactory.unknownType();
    }
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
    throw JsonMappingException.from(ctxt, "Could not instantiate bean,  " + entityType + " is not a Scout bean");
  }
}
