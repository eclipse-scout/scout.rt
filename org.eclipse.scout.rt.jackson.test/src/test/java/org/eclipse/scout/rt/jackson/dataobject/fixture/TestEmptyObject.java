/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * Custom serializer writing completely empty object
 */
class TestEmptyObjectSerializer extends StdScalarSerializer<TestEmptyObject> {
  private static final long serialVersionUID = 1L;

  public TestEmptyObjectSerializer() {
    super(TestEmptyObject.class);
  }

  @Override
  public void serialize(TestEmptyObject value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartObject();
    gen.writeEndObject();
  }
}

/**
 * Test object being serialized as empty JSON object "{ }"
 */
@JsonSerialize(using = TestEmptyObjectSerializer.class)
public class TestEmptyObject {
}
