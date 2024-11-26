/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;

import org.eclipse.scout.rt.platform.resource.BinaryResource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class DoBinaryResourceSerializer extends StdSerializer<BinaryResource> {

  private static final long serialVersionUID = 1L;

  protected DoBinaryResourceSerializer() {
    super(BinaryResource.class);
  }

  // TODO pbz check how we can do this only for DOs but not for all BinaryResources globally

  @Override
  public void serialize(BinaryResource br, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartObject();
    writeNullsafeBinaryField(gen, "content", br.getContent());
    gen.writeNumberField("contentLength", br.getContentLength());
    gen.writeNumberField("lastModified", br.getLastModified());
    gen.writeStringField("contentType", br.getContentType());
    gen.writeStringField("filename", br.getFilename());
    gen.writeStringField("charset", br.getCharset());
    gen.writeNumberField("fingerprint", br.getFingerprint());
    gen.writeBooleanField("cachingAllowed", br.isCachingAllowed());
    gen.writeNumberField("cacheMaxAge", br.getCacheMaxAge());
    gen.writeEndObject();
  }

  protected void writeNullsafeBinaryField(JsonGenerator gen, String fieldName, byte[] data) throws IOException {
    if (data == null) {
      gen.writeNullField(fieldName);
    }
    else {
      gen.writeBinaryField(fieldName, data);
    }
  }

}
