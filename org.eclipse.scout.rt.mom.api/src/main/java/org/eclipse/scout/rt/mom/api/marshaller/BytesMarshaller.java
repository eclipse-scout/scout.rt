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

import static org.eclipse.scout.rt.platform.util.Assertions.assertInstance;

import java.util.Map;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * This marshaller allows to transport an array of bytes across the network. It does not support other data types.
 * <p>
 * This marshaller does not support serialization of exceptions.
 *
 * @see IMarshaller#MESSAGE_TYPE_BYTES
 * @since 6.1
 */
@Bean
public class BytesMarshaller implements IMarshaller {

  /**
   * @param transferObject
   *     object to marshal, must be of type {@code byte[]} (or {@code null})
   * @throws AssertionException
   *     if the given object is not of the expected type
   */
  @Override
  public Object marshall(final Object transferObject, final Map<String, String> context) {
    if (transferObject == null) {
      return null;
    }
    return assertInstance(transferObject, byte[].class, "bytes array expected [actual={}]", transferObject.getClass().getSimpleName());
  }

  @Override
  public Object unmarshall(final Object data, final Map<String, String> context) {
    return data;
  }

  @Override
  public int getMessageType() {
    return MESSAGE_TYPE_BYTES;
  }
}
