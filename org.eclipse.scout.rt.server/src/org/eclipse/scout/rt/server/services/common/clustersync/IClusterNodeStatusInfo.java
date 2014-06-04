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

import java.util.Date;

/**
 *
 */
public interface IClusterNodeStatusInfo {

  /**
   * @return number of sent messages
   */
  long getSentMessageCount();

  /**
   * @return number of received messages
   */
  long getReceivedMessageCount();

  /**
   * @return date of the last received message
   */
  Date getLastReceivedDate();

  /**
   * @return user id of the last received message
   */
  String getLastReceivedOriginUser();

  /**
   * @return node id of the last received message
   */
  String getLastReceivedOriginNode();

}
