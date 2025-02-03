/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel;

import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.shared.SharedConfigProperties.CompressServiceTunnelRequestProperty;

abstract class AbstractServiceTunnelContentHandler implements IServiceTunnelContentHandler {

  private IObjectSerializer m_objectSerializer;
  private Boolean m_sendCompressed;

  @Override
  public void initialize() {
    m_sendCompressed = CONFIG.getPropertyValue(CompressServiceTunnelRequestProperty.class);
    m_objectSerializer = createObjectSerializer();
  }

  /**
   * @return Creates an {@link IObjectSerializer} instance used for serializing and deserializing data.
   * @since 3.8.2
   */
  protected IObjectSerializer createObjectSerializer() {
    return SerializationUtility.createObjectSerializer(new ServiceTunnelObjectReplacer());
  }

  protected Boolean isSendCompressed() {
    return m_sendCompressed;
  }

  protected IObjectSerializer getObjectSerializer() {
    return m_objectSerializer;
  }
}
