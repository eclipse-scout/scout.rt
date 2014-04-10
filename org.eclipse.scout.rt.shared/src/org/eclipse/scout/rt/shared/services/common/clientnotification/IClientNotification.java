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
package org.eclipse.scout.rt.shared.services.common.clientnotification;

import java.io.Serializable;

/**
 * Client notifications are used to trigger events from the server to the client
 * (reverse HTTP) <br>
 * These notifications are collected on the server side in a {@link ClientNotificationQueue} /
 * {@link J2eeClientNotificationQueue} and
 * fetched by the {@link ClientNotificationConsumer} using {@link IClientNotificationService} with intelligent polling
 * Note that client
 * notifications must be serializable because they are transferred between
 * server and client. <br>
 * All client notifications must therefore be defined in "shared" plugins known
 * by server and client.
 * <p>
 * Examples:
 * <code>AccessControlChangedNotification, SharedContextChangedNotification, CodeTypeChangedNotification</code>
 */
public interface IClientNotification extends Serializable {

  /**
   * @return the unique id of this notification.
   */
  String getId();

  /**
   * Merge with other notifications of the same type Same type means
   * n1.getClass()==n2.getClass()
   * 
   * @return true if existingNotification was coalesced and therefore is
   *         consumed. The existingNotification is then removed from the queue.
   */
  boolean coalesce(IClientNotification existingNotification);

  /**
   * Gets the node where the notification is orignially fired. This is for
   * cluster environment.
   * 
   * @return node (the property com.bsiag.crm.server#nodeId is one of
   *         com.bsiag.crm.server#nodeId1...com.bsiag.crm.server#nodeIdn)
   */
  int getOriginNode();

  /**
   * Sets the node where the notification is orignially fired. This is for
   * cluster environment.
   * 
   * @param node
   *          (the property com.bsiag.crm.server#nodeId is one of
   *          com.bsiag.crm.server#nodeId1...com.bsiag.crm.server#nodeIdn)
   */
  void setOriginNode(int node);
}
