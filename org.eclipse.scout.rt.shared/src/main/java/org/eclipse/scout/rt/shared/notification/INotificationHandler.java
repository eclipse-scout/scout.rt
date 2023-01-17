/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.notification;

import java.io.Serializable;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * Notification handler for notifications of a given type. A registration of an {@link INotificationHandler} is not
 * needed. All handlers will be picked up using the {@link BEANS#all(INotificationHandler)}.
 * <h3>Client notifications</h3> A client notification addressed to users, sessions (also all sessions and all users)
 * will be executed in a client run context having the corresponding client session on the thread context. A
 * notification addressed to one or many nodes (nodeId) will be executed in a empty run context with no access to a
 * certain session.
 *
 * @param T
 *          the type of the notification
 * @see NotificationHandlerRegistry
 */
@FunctionalInterface
@ApplicationScoped
public interface INotificationHandler<T extends Serializable> {

  /**
   * Handle notifications of type T
   *
   * @param notification
   */
  void handleNotification(T notification);
}
