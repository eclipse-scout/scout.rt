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
package org.eclipse.scout.rt.server.jaxws.consumer;

import java.util.List;

import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

/**
 * Classes implementing this interface provide web service ports. Whether these ports are created directly when
 * requested, taken from a stash or a pool is implementer specific. Ports are never returned explicitly to the provider.
 * Pool implementations are required to use an {@link org.eclipse.scout.rt.platform.transaction.ITransactionMember}.
 *
 * @since 6.0.300
 */
public interface IPortProvider<PORT> {

  /**
   * @return Returns a port ready-to-be-used by the caller.
   */
  PORT provide();

  /**
   * Callback to initialize a Port.
   */
  public interface IPortInitializer {

    /**
     * Method invoked to install handler in the given handler-chain.
     */
    void initHandlers(List<Handler<? extends MessageContext>> handlerChain);

    /**
     * Method invoked to enable implementor specific webservice features.
     */
    void initWebServiceFeatures(List<WebServiceFeature> webServiceFeatures);
  }
}
