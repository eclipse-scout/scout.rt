/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * Listener to receive messages.
 *
 * @see IMom#subscribe(IDestination, IMessageListener, RunContext)
 * @since 6.1
 */
public interface IMessageListener<TRANSFER_OBJECT> {

  /**
   * Method invoked upon the receive of a message, and is invoked in the {@link RunContext} as specified at
   * registration.
   */
  void onMessage(IMessage<TRANSFER_OBJECT> message);
}
