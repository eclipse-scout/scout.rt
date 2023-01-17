/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.api.marshaller;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.scout.rt.platform.Bean;

/**
 * This marshaller allows to transport an object's JSON representation as binary data across the network.
 *
 * @see IMarshaller#MESSAGE_TYPE_BYTES
 * @since 6.1
 */
@Bean
public class JsonAsBytesMarshaller extends JsonMarshaller {

  @Override
  public Object marshall(final Object transferObject, final Map<String, String> context) {
    final String jsonText = (String) super.marshall(transferObject, context);
    if (jsonText == null) {
      return null;
    }

    return jsonText.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public Object unmarshall(final Object data, final Map<String, String> context) {
    final byte[] jsonBytes = (byte[]) data;
    if (jsonBytes == null) {
      return null;
    }

    final String jsonText = new String(jsonBytes, StandardCharsets.UTF_8);
    return super.unmarshall(jsonText, context);
  }

  @Override
  public int getMessageType() {
    return MESSAGE_TYPE_BYTES;
  }
}
