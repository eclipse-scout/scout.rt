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

import org.eclipse.scout.rt.platform.service.IService;

/**
 * The {@link IClusterNotificationListener} provided by this interface is automatically added through
 * {@link IClusterSynchronizationService#addListener(IClusterNotificationListener)} when the cluster synchronization
 * service is enabled ({@link IClusterSynchronizationService#enable()}).
 *
 * @deprecated use {@link INotificationHandler<>}
 */
@Deprecated
public interface IClusterNotificationListenerService extends IService {

  /**
   * @return {@link IClusterNotificationListener} to be added to {@link IClusterNotificationListenerService}
   */
  IClusterNotificationListener getClusterNotificationListener();
}
