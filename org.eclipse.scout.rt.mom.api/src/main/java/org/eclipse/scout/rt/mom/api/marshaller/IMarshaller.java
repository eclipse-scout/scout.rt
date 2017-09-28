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

/**
 * Transforms an object into its transport type to transmit it across the network.
 *
 * @since 6.1
 */
public interface IMarshaller {

  /**
   * Indicates to transport data as text message.
   */
  int MESSAGE_TYPE_TEXT = 1;

  /**
   * Indicates to transport data as binary message.
   */
  int MESSAGE_TYPE_BYTES = 2;

  /**
   * Indicates that no payload data is transferred.
   */
  int MESSAGE_TYPE_NO_PAYLOAD = 3;

  /**
   * Marshalls the given transfer object into its transport type to be published.
   */
  Object marshall(Object transferObject, Map<String, String> context);

  /**
   * Unmarshalls the given transport data into its object type.
   */
  Object unmarshall(Object data, Map<String, String> context);

  /**
   * Returns the message type to be used, e.g. a text message for textual data, or a binary message for binary data.
   *
   * @see #MESSAGE_TYPE_TEXT
   * @see #MESSAGE_TYPE_BYTES
   */
  int getMessageType();
}
