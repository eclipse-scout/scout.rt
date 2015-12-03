/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
