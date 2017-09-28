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
 * Listener to receive messages.
 *
 * @see IMom#subscribe(IDestination, IMessageListener, RunContext)
 * @since 6.1
 */
@FunctionalInterface
public interface IMessageListener<DTO> {

  /**
   * Method invoked upon the receive of a message, and is invoked in the {@link RunContext} as specified at
   * registration.
   */
  void onMessage(IMessage<DTO> message);
}
