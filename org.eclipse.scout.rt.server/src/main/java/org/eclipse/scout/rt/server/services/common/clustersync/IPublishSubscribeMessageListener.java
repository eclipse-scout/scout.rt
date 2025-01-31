/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.clustersync;

@FunctionalInterface
public interface IPublishSubscribeMessageListener {

  /**
   * Method invoked to handle a message received from a subscribed channel.
   *
   * @param message
   *     message received.
   */
  void onMessage(IClusterNotificationMessage message);
}
