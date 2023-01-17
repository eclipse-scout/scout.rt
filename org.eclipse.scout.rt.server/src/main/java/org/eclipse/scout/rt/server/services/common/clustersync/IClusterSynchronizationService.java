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

import java.io.Serializable;

import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.service.IService;

/**
 * Service for synchronizing server nodes by sending messages and receiving messages.
 */
public interface IClusterSynchronizationService extends IService {

  /**
   * Indicates the order of the cluster synchronization's {@link IPlatformListener} to shutdown itself upon entering
   * platform state {@link State#PlatformStopping}. Any listener depending on cluster synchronization facility must be
   * configured with an order less than {@link #DESTROY_ORDER}.
   */
  long DESTROY_ORDER = 5_700;

  /**
   * Publish a message with the given notification for the other server nodes.
   */
  void publish(Serializable notification);

  /**
   * Publish a message with the given notification for the other server nodes, if the transaction is committed.
   */
  void publishTransactional(Serializable notification);

  /**
   * @return info about sent and received messages
   */
  IClusterNodeStatusInfo getStatusInfo();

  /**
   * @return info about sent and received messages of a given message type
   */
  IClusterNodeStatusInfo getStatusInfo(Class<? extends Serializable> messageType);

  IClusterNotificationProperties getNotificationProperties();

  /**
   * Starts listening to notifications
   *
   * @return <code>true</code>, if successful
   */
  boolean enable();

  /**
   * Stops listening to notifications
   *
   * @return <code>true</code>, if successful
   */
  boolean disable();

  /**
   * @return <code>true</code>, if started and listening to cluster notifications
   */
  boolean isEnabled();

}
