package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;

import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.IDataObject;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/**
 * Generic Deserializer for {@link IDataObject} delegating to {@link DoEntityDeserializer} / {@link DoListDeserializer}
 * according to content.
 */
public class DataObjectDeserializer extends StdDeserializer<IDataObject> {
  private static final long serialVersionUID = 1L;

  public DataObjectDeserializer(Class<?> type) {
    super(type);
  }

  @Override
  public IDataObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return deserializeDataObject(p, ctxt, null);
  }

  @Override
  public IDataObject deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
    return deserializeDataObject(p, ctxt, typeDeserializer);
  }

  protected IDataObject deserializeDataObject(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
    switch (p.currentToken()) {
      case START_OBJECT:
        return p.getCodec().readValue(p, IDoEntity.class); // delegate to DoEntityDeserializer for object-like structure
      case START_ARRAY:
        return p.getCodec().readValue(p, DoList.class); // delegate to DoListDeserializer for collection-like structure
      default:
        throw ctxt.wrongTokenException(p, handledType(), JsonToken.START_OBJECT, "expected start object or start array token");
    }
  }
}
