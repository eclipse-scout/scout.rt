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
package org.eclipse.scout.rt.shared.services.common.offline;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.rt.shared.validate.InputValidation;

/**
 * This service is representing a local server on the frontend used to process
 * server logic similiar to the backend in transactions and with transaction support. <br>
 */
@InputValidation(IValidationStrategy.PROCESS.class)
public interface IOfflineDispatcherService extends IService {

  IServiceTunnelResponse dispatch(final ServiceTunnelRequest request);
}
