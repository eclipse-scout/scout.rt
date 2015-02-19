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
package org.eclipse.scout.rt.shared.services.common.useractivity;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.IService;

/**
 * Register this service as a scout server service (as a per-user-session
 * service)
 */
@Priority(-3)
public interface IUserActivityStateService extends IService {
  int STATUS_OFFLINE = 1;
  int STATUS_ONLINE = 2;
  int STATUS_IDLE = 3;

  /**
   * Change the status of the current user (user session holding this service)
   */
  void setStatus(int status) throws ProcessingException;

  /**
   * get the state of all known users
   */
  UserStatusMap getUserStatusMap() throws ProcessingException;
}
