/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
