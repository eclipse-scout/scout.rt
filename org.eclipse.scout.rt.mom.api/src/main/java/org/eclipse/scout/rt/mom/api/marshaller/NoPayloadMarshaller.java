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

import java.util.Map;

import org.eclipse.scout.rt.platform.Bean;

/**
 * This marshaller indicates that no payload is transfered and nothing has to be marshalled.
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
