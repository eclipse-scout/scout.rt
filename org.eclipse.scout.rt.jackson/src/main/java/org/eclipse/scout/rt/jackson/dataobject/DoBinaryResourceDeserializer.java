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
    if (field == null) {
      return null;
    }
    return field.isNull() ? null : field.asText();
  }

  @Override
  public BinaryResource deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);

    // Note: the properties contentLength and fingerprint, are calculated based on the content
    // So we don't need to set it here. However they are added to the JSON, because they might
    // provide some information about the data.
    BinaryResources builder = BinaryResources.create();

    JsonNode fieldNode = node.get("content");
    if (fieldNode != null) {
      builder.withContent(fieldNode.binaryValue());
    }
    fieldNode = node.get("lastModified");
    if (fieldNode != null) {
      builder.withLastModified(fieldNode.asLong());
    }
    builder
        .withContentType(getNullableText(node, "contentType"))
        .withFilename(getNullableText(node, "filename"))
        .withCharset(getNullableText(node, "charset"));
    fieldNode = node.get("cachingAllowed");
    if (fieldNode != null) {
      builder.withCachingAllowed(fieldNode.asBoolean());
    }
    fieldNode = node.get("cacheMaxAge");
    if (fieldNode != null) {
      builder.withCacheMaxAge(fieldNode.asInt());
    }

    return builder.build();
  }
}
