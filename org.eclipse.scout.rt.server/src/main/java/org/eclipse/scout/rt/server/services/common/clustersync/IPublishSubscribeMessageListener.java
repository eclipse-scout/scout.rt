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
package org.eclipse.scout.rt.server.services.common.clustersync;

@FunctionalInterface
public interface IPublishSubscribeMessageListener {

  /**
   * Method invoked to handle a message received from a subscribed channel.
   *
   * @param message
   *          message received.
   */
  void onMessage(IClusterNotificationMessage message);
}
