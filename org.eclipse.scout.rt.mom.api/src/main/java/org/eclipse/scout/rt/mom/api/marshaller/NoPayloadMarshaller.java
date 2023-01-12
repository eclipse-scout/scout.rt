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

import java.util.Map;

import org.eclipse.scout.rt.platform.Bean;

/**
 * This marshaller indicates that no payload is transferred and nothing has to be marshalled.
 * <p>
 * This marshaller does not support the serialization of exceptions.
 *
 * @see IMarshaller#MESSAGE_TYPE_NO_PAYLOAD
 * @since 7.0
 */
@Bean
public class NoPayloadMarshaller implements IMarshaller {

  @Override
  public Object marshall(final Object transferObject, final Map<String, String> context) {
    return null;
  }

  @Override
  public Object unmarshall(final Object data, final Map<String, String> context) {
    return null;
  }

  @Override
  public int getMessageType() {
    return IMarshaller.MESSAGE_TYPE_NO_PAYLOAD;
  }
}
