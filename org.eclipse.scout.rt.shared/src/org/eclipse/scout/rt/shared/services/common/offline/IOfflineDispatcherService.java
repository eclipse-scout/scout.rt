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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.service.IService;

/**
 * This service is representing a local server on the frontend used to process
 * server logic similiar to the backend in transactions and with xa support. <br>
 * Normally an implementation of this service such as OfflineDispatcherService
 * is registered in the ...server.offline plugin's plugin.xml in the
 * org.eclipse.scout.services extension.
 */
@Priority(-3)
public interface IOfflineDispatcherService extends IService {

  String getServerSessionClass();

  void setServerSessionClass(String className);

  ServiceTunnelResponse dispatch(final ServiceTunnelRequest request, final IProgressMonitor prog);
}
