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

import org.eclipse.scout.rt.server.ServerJob;

public interface IPublishSubscribeMessageListener {

  /**
   * This method is <strong>not</strong> called within a scout transaction. Therefore a message listener should insure
   * that a new {@link ServerJob} is created and the message is handled in this job.
   * 
   * @param message
   */
  void onMessage(IClusterNotificationMessage message);
}
