/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel;

import java.util.EventListener;

/**
 * This is a content observer that allows for adding content validation listeners that can filter and check all
 * transferred objects.
 */
public interface IServiceTunnelContentObserver {

  public interface IInboundListener extends EventListener {
    /**
     * This callback is invoked immediately for every object being unmarshalled (deserialized)
     * 
     * @throws an
     *           exception when a check or filter fails (value inconsistent, string too long, etc.)
     */
    void filterInbound(Object o) throws Exception;
  }

  public interface IOutboundListener extends EventListener {
    /**
     * This callback is invoked immediately for every object being marshalled (serialized)
     * 
     * @throws an
     *           exception when a check or filter fails (value inconsistent, string too long, etc.)
     */
    void filterOutbound(Object o) throws Exception;
  }

  void addInboundListener(IInboundListener listener);

  void removeInboundListener(IInboundListener listener);

  void addOutboundListener(IOutboundListener listener);

  void removeOutboundListener(IOutboundListener listener);

}
