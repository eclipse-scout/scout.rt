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

import java.util.Date;

/**
 * Some information about current status of the cluster sync.
 */
public interface IClusterNodeStatusInfo {

  /**
   * @return last update of the status
   */
  Date getLastChangedDate();

  /**
   * @return user that last updated the status
   */
  String getLastChangedUserId();

  /**
   * @return node that last updated the status
   */
  String getLastChangedOriginNodeId();

  /**
   * @return number of sent messages
   */
  long getSentMessageCount();

  /**
   * @return number of received messages
   */
  long getReceivedMessageCount();

}
