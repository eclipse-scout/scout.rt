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
package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * Listener to receive requests in 'request-reply' communication.
 *
 * @see IMom#reply(IDestination, IRequestListener, RunContext)l
 * @since 6.1
 */
@FunctionalInterface
public interface IRequestListener<REQUEST_OBJECT, REPLY_OBJECT> {

  /**
   * Method invoked upon the receive of a request in 'request-reply' communication, and is invoked in the
   * {@link RunContext} as specified at registration.
   * <p>
   * If the initiator is interrupted while waiting for the reply, this replying thread is interrupted accordingly.
   *
   * @return reply to be sent to the initiator of this 'request-reply' communication.
   */
  REPLY_OBJECT onRequest(IMessage<REQUEST_OBJECT> request);
}
