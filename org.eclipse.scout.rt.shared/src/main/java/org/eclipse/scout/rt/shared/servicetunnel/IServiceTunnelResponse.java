/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel;

import java.io.Serializable;
import java.util.Set;

import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 *
 */
public interface IServiceTunnelResponse extends Serializable {

  Object getData();

  Object[] getOutVars();

  Throwable getException();

  Long getProcessingDuration();

  /**
   * @return
   */
  Set<ClientNotificationMessage> getNotifications();

  /**
   * @param notifications
   */
  void setNotifications(Set<ClientNotificationMessage> notifications);

}
