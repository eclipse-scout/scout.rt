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

import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

/**
 *
 */
public interface IServiceTunnelResponse extends Serializable {

  String getSoapOperation();

  /**
   * @return 0 if code is unknown or >0 if code is known
   */
  int getHttpCode();

  Object getData();

  Object[] getOutVars();

  Throwable getException();

  Set<IClientNotification> getClientNotifications();

  Long getProcessingDuration();

}
