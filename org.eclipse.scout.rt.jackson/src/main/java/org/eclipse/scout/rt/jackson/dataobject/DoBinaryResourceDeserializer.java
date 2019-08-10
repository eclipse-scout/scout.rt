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
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class DoBinaryResourceDeserializer extends StdDeserializer<BinaryResource> {

  private static final long serialVersionUID = 1L;

  protected DoBinaryResourceDeserializer() {
    super(BinaryResource.class);
  }

  /**
   * Prevent null values from being read as String with content "null".
   */
  protected String getNullableText(JsonNode node, String fieldName) {
    JsonNode field = node.get(fieldName);
    return field.isNull() ? null : field.asText();
  }

  @Override
  public BinaryResource deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    byte[] content = node.get("content").binaryValue();
    long lastModified = node.get("lastModified").asLong();
    String contentType = getNullableText(node, "contentType");
    String filename = getNullableText(node, "filename");
    String charset = getNullableText(node, "charset");
    boolean cachingAllowed = node.get("cachingAllowed").asBoolean();
    int cacheMaxAge = node.get("cacheMaxAge").asInt();

    // Note: the properties contentLength and fingerprint, are calculated based on the content
    // So we don't need to set it here. However they are added to the JSON, because they might
    // provide some information about the data.
    return BinaryResources.create()
        .withContent(content)
        .withLastModified(lastModified)
        .withContentType(contentType)
        .withFilename(filename)
        .withCharset(charset)
        .withCachingAllowed(cachingAllowed)
        .withCacheMaxAge(cacheMaxAge)
        .build();
  }

}
