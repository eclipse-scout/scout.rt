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

import java.util.Date;

import org.eclipse.scout.rt.dataobject.id.NodeId;

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
  NodeId getLastChangedOriginNodeId();

  /**
   * @return number of sent messages
   */
  long getSentMessageCount();

  /**
   * @return number of received messages
   */
  long getReceivedMessageCount();
}
