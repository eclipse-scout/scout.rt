/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.api.marshaller;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.scout.rt.platform.Bean;

/**
 * This marshaller allows to transport an object's {@link #toString()} representation as binary data across the network.
 * <p>
 * This marshaller does not support the serialization of exceptions.
 * 
 * @see IMarshaller#MESSAGE_TYPE_BYTES
 * @since 6.1
 */
@Bean
public class TextAsBytesMarshaller extends TextMarshaller {

  @Override
  public Object marshall(final Object transferObject, final Map<String, String> context) {
    final String plainText = (String) super.marshall(transferObject, context);
    if (plainText == null) {
      return null;
    }

    return plainText.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public Object unmarshall(final Object data, final Map<String, String> context) {
    final byte[] plainBytes = (byte[]) data;
    if (plainBytes == null) {
      return null;
    }

    final String plainText = new String(plainBytes, StandardCharsets.UTF_8);
    return super.unmarshall(plainText, context);
  }

  @Override
  public int getMessageType() {
    return IMarshaller.MESSAGE_TYPE_BYTES;
  }
}
