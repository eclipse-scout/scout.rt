/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.StdScalarSerializer;

/**
 * Custom serializer writing completely empty object
 */
class TestEmptyObjectSerializer extends StdScalarSerializer<TestEmptyObject> {

  public TestEmptyObjectSerializer() {
    super(TestEmptyObject.class);
  }

  @Override
  public void serialize(TestEmptyObject value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
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
