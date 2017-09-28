/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.api.marshaller;

import java.io.IOException;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;

/**
 * This marshaller allows to transport an object's serialized representation as binary data across the network.
 *
 * @see IMarshaller#MESSAGE_TYPE_BYTES
 * @since 6.1
 */
@Bean
public class ObjectMarshaller implements IMarshaller {

  protected static final String CTX_PROP_OBJECT_TYPE = "x-scout.mom.object.objecttype";

  @Override
  public Object marshall(final Object transferObject, final Map<String, String> context) {
    if (transferObject == null) {
      return null;
    }

    try {
      final IObjectSerializer serializer = SerializationUtility.createObjectSerializer();
      final byte[] bytes = serializer.serialize(transferObject);

      context.put(CTX_PROP_OBJECT_TYPE, transferObject.getClass().getName());
      return bytes;
    }
    catch (final IOException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public Object unmarshall(final Object data, final Map<String, String> context) {
    final byte[] bytes = (byte[]) data;
    if (bytes == null) {
      return null;
    }

    try {
      final Class<?> objectType = Class.forName(context.get(CTX_PROP_OBJECT_TYPE));
      final IObjectSerializer serializer = SerializationUtility.createObjectSerializer();
      return serializer.deserialize(bytes, objectType);
    }
    catch (final IOException | ClassNotFoundException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public int getMessageType() {
    return MESSAGE_TYPE_BYTES;
  }
}
