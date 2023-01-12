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
 * This marshaller allows to transport an object's {@link #toString()} representation as textual data across the
 * network.
 * <p>
 * This marshaller does not support the serialization of exceptions.
 *
 * @see IMarshaller#MESSAGE_TYPE_TEXT
 * @since 6.1
 */
@Bean
public class TextMarshaller implements IMarshaller {

  @Override
  public Object marshall(final Object transferObject, final Map<String, String> context) {
    if (transferObject == null) {
      return null;
    }

    return transferObject.toString();
  }

  @Override
  public Object unmarshall(final Object data, final Map<String, String> context) {
    return data;
  }

  @Override
  public int getMessageType() {
    return IMarshaller.MESSAGE_TYPE_TEXT;
  }
}
