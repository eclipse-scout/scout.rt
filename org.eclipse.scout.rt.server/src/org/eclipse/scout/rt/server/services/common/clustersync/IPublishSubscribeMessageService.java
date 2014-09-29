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
package org.eclipse.scout.rt.server.services.common.clustersync;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.IService;

/**
 * Note: Implementing services must not be registered with a session based service factory.
 */
public interface IPublishSubscribeMessageService extends IService {

  void setListener(IPublishSubscribeMessageListener listener);

  IPublishSubscribeMessageListener getListener();

  void subscribe() throws ProcessingException;

  void unsubsribe() throws ProcessingException;

  /**
   * This method is not called within a scout server transaction
   */
  void publishNotifications(List<IClusterNotificationMessage> notificationMessages);
}
