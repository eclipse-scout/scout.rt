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
package org.eclipse.scout.rt.shared.services.common.processing;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.rt.shared.validate.InputValidation;

@TunnelToServer
public interface IServerProcessingCancelService extends IService {

  /**
   * Cancels a backend job with its associated transaction which was originally initiated by a client-server request.<br/>
   * Also, any nested 'runNow'-style jobs, which where run on behalf of that job and did not complete yet, are
   * cancelled, as well as any associated transactions. In order to be cancelled, the session of the cancel-request must
   * be the same as the job's session.
   *
   * @param requestSequence
   *          id of the job; corresponds to the <code>requestSequence</code> of the {@link ServiceTunnelRequest} which
   *          initiated the job.
   * @return <code>true</code> if cancel was successful and transaction was in fact cancelled, <code>false</code>
   *         otherwise.
   */
  @InputValidation(IValidationStrategy.NO_CHECK.class)
  boolean cancel(long requestSequence);
}
