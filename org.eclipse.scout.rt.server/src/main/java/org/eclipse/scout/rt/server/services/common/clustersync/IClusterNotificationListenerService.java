/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.clustersync;

import org.eclipse.scout.service.IService;

/**
 * The {@link IClusterNotificationListener} provided by this interface is automatically added
 * through {@link IClusterSynchronizationService#addListener(IClusterNotificationListener)} when the cluster
 * synchronization service is enabled ({@link IClusterSynchronizationService#enable()}).
 */
public interface IClusterNotificationListenerService extends IService {

  /**
   * The defining service interface is used to avoid adding overridden service registrations as listeners.
   * <p>
   * Therefore only one service with the highest ranking with respect to the defining service interface is added as
   * listener.
   *
   * @return service interface - not null
   */
  Class<? extends IService> getDefiningServiceInterface();

  /**
   * @return {@link IClusterNotificationListener} to be added to {@link IClusterNotificationListenerService}
   */
  IClusterNotificationListener getClusterNotificationListener();
}
