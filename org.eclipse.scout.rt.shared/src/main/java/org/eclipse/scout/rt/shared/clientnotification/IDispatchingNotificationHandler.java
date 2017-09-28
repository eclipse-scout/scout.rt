/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.clientnotification;

import java.io.Serializable;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Notification handler that is capable of dispaching based on the {@link IClientNotificationAddress}.
 */
@FunctionalInterface
@ApplicationScoped
public interface IDispatchingNotificationHandler<T extends Serializable> {

  /**
   * @param notification
   * @param address
   */
  void handleNotification(T notification, IClientNotificationAddress address);

}
