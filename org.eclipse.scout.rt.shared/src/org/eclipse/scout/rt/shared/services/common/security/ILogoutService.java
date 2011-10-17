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
package org.eclipse.scout.rt.shared.services.common.security;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.shared.validate.InputValidation;
import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.service.IService;

/**
 * Support service to explicitly close a session and release cached sessions, resources, and credentials
 */
@Priority(-3)
@InputValidation(IValidationStrategy.PROCESS.class)
public interface ILogoutService extends IService {

  /**
   * calling this remote service on the server causes the session to be closed and resources to be cleaned, a new
   * session and new login will be needed to continue working
   */
  void logout();

}
