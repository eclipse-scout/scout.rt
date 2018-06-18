/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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

  // FIXME [pbz] check how we can do this only for DOs but not for all BinaryResources globally

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
