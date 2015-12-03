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

import java.util.List;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.service.IService;

/**
 * Note: Implementing services must not be registered with a session based service factory.
 */
public interface IPublishSubscribeMessageService extends IService {

  void setListener(IPublishSubscribeMessageListener listener);

  IPublishSubscribeMessageListener getListener();

  /**
   * Initialize the service and start listening to events. Usually one should set the listener
   * {@link #setListener(IPublishSubscribeMessageListener)} before calling subscribe.
   *
   * @throws ProcessingException
   *           if because of some reason the service could not be fully initialized
   */
  void subscribe();

  void unsubsribe();

  /**
   * This method is not called within a scout server transaction
   */
  void publishNotifications(List<IClusterNotificationMessage> notificationMessages);
}
