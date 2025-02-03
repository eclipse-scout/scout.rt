/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.clientnotification;

import java.io.Serializable;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Notification handler that is capable of dispatching based on the {@link IClientNotificationAddress}.
 */
@FunctionalInterface
@ApplicationScoped
public interface IDispatchingNotificationHandler<T extends Serializable> {

  void handleNotification(T notification, IClientNotificationAddress address);
}
