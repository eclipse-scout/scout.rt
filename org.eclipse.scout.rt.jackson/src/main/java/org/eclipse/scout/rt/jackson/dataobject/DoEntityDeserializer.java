package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
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
public class DoEntityDeserializer extends StdDeserializer<DoEntity> {
  private static final long serialVersionUID = 1L;

  protected final LazyValue<DataObjectDefinitionRegistry> m_doEntityDefinitionRegistry = new LazyValue<>(DataObjectDefinitionRegistry.class);

  public DoEntityDeserializer(Class<?> type) {
    super(type);
  }

  @Override
  public DoEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return deserializeDoEntity(p, ctxt, null);
  }

  @Override
  public DoEntity deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
    return deserializeDoEntity(p, ctxt, typeDeserializer);
  }

  protected DoEntity deserializeDoEntity(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
    switch (p.nextToken()) {
      case FIELD_NAME:
        return deserializeDoEntityAttributes(p, ctxt);
      case END_OBJECT:
        // empty object without attributes consists only of START_OBJECT and END_OBJECT token, return empty entity object
        return (DoEntity) newObject(ctxt, handledType());
      default:
        throw ctxt.wrongTokenException(p, handledType(), JsonToken.FIELD_NAME, null);
    }
  }

  @SuppressWarnings({"resource", "squid:S2095"})
  protected DoEntity deserializeDoEntityAttributes(JsonParser p, DeserializationContext ctxt) throws IOException {
    // search for type property within attributes of DoEntity, cache other fields within token buffer
    TokenBuffer tb = null;
    for (JsonToken t = p.currentToken(); t == JsonToken.FIELD_NAME; t = p.nextToken()) {
      String attributeName = p.getCurrentName();
      p.nextToken(); // let current token point to the value

      // check if found the type property
      if (DataObjectTypeResolverBuilder.JSON_TYPE_PROPERTY.equals(attributeName)) {
        String entityType = p.getText();

        // put back the cached fields of token buffer to parser, if any fields were cached while searching type property
        if (tb != null) {
          p.clearCurrentToken();
          p = JsonParserSequence.createFlattened(false, tb.asParser(p), p);
        }
        p.nextToken(); // skip type field value
        return derializeDoEntityAttributes(p, ctxt, entityType);
      }

      // lazy create token buffer to cache other fields
      if (tb == null) {
        tb = new TokenBuffer(p, ctxt);
      }

      // write current field name and value to token buffer for later parsing
      tb.writeFieldName(attributeName);
      tb.copyCurrentStructure(p);
    }

    // no type information found within fields, if any fields where cached, finish token buffer and move parser back to cached token buffer fields
    if (tb != null) {
      tb.writeEndObject();
      p = tb.asParser(p);
      // initialize parser pointing to first field token
      p.nextToken();
    }
    return derializeDoEntityAttributes(p, ctxt, null); // null = use default entity type
  }

  protected DoEntity derializeDoEntityAttributes(JsonParser p, DeserializationContext ctxt, String entityType) throws IOException {
    DoEntity entity = resolveEntityType(ctxt, entityType);
    p.setCurrentValue(entity);

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

  protected DoEntity resolveEntityType(DeserializationContext ctxt, String entityType) throws IOException {
    if (entityType != null) {
      // try to lookup DoEntity with specified entityType
      Class<?> clazz = m_doEntityDefinitionRegistry.get().fromTypeName(entityType);
      if (clazz != null) {
        return (DoEntity) newObject(ctxt, clazz);
      }
      else {
        // use generic DoTypedEntity to preserve type information even if correct DoEntity class could not be resolved
        return newObject(ctxt, DoTypedEntity.class).withType(entityType);
      }
    }
    // fallback to handled type of deserializer
    return (DoEntity) newObject(ctxt, handledType());
  }

  protected JavaType findResolvedAttributeType(DoEntity entityInstance, String attributeName, boolean isObject, boolean isArray) {
    return m_doEntityDefinitionRegistry.get().getAttributeDescription(entityInstance.getClass(), attributeName)
        .map(DataObjectAttributeDefinition::getType)
        .orElseGet(() -> findResolvedFallbackAttributeType(isObject, isArray));
  }

  protected JavaType findResolvedFallbackAttributeType(boolean isObject, boolean isArray) {
    // fallback to default handling, if no attribute definition could be found
    if (isObject) {
      // all object-like JSON structure is deserialized as raw DoEntity
      return TypeFactory.defaultInstance().constructType(DoEntity.class);
    }
    else if (isArray) {
      // all array-like JSON structure is deserialized as raw DoList
      return TypeFactory.defaultInstance().constructType(DoList.class);
    }
    else {
      // all JSON scalar values are deserialized as raw object using default jackson typing
      return TypeFactory.unknownType();
    }
  }

  protected <T> T newObject(DeserializationContext ctxt, Class<T> c) throws IOException {
    if (BEANS.getBeanManager().isBean(c)) {
      return BEANS.get(c);
    }
    throw JsonMappingException.from(ctxt, "Could not instantiate bean,  " + c + " is not a Scout bean");
  }
}
