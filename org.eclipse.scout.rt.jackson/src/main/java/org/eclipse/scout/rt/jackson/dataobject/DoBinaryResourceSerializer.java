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

import org.eclipse.scout.rt.platform.resource.BinaryResource;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class DoBinaryResourceSerializer extends StdSerializer<BinaryResource> {

  protected DoBinaryResourceSerializer() {
    super(BinaryResource.class);
  }

  // TODO pbz check how we can do this only for DOs but not for all BinaryResources globally

  @Override
  public void serialize(BinaryResource br, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
    gen.writeStartObject();
    writeNullsafeBinaryField(gen, "content", br.getContent());
    gen.writeNumberProperty("contentLength", br.getContentLength());
    gen.writeNumberProperty("lastModified", br.getLastModified());
    gen.writeStringProperty("contentType", br.getContentType());
    gen.writeStringProperty("filename", br.getFilename());
    gen.writeStringProperty("charset", br.getCharset());
    gen.writeNumberProperty("fingerprint", br.getFingerprint());
    gen.writeBooleanProperty("cachingAllowed", br.isCachingAllowed());
    gen.writeNumberProperty("cacheMaxAge", br.getCacheMaxAge());
    gen.writeEndObject();
  }

  protected void writeNullsafeBinaryField(JsonGenerator gen, String fieldName, byte[] data) throws JacksonException {
    if (data == null) {
      gen.writeNullProperty(fieldName);
    }
    else {
      gen.writeBinaryProperty(fieldName, data);
    }
  }
}
