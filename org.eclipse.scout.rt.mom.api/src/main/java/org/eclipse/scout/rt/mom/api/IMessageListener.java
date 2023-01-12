/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * Listener to receive messages.
 *
 * @see IMom#subscribe(IDestination, IMessageListener, SubscribeInput)
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
