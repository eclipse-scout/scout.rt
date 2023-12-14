/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.consumer;

import java.util.List;

import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.MessageContext;

/**
 * Classes implementing this interface provide web service ports. Whether these ports are created directly when
 * requested, taken from a stash or a pool is implementer specific. Ports are never returned explicitly to the provider.
 * Pool implementations are required to use an {@link org.eclipse.scout.rt.platform.transaction.ITransactionMember}.
 *
 * @since 6.0.300
 */
@FunctionalInterface
public interface IPortProvider<PORT> {

  /**
   * @return Returns a port ready-to-be-used by the caller.
   */
  PORT provide();

  /**
   * Callback to initialize a Port.
   */
  interface IPortInitializer {

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
