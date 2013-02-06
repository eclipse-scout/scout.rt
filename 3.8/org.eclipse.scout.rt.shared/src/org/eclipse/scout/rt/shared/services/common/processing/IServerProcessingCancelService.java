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

import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.rt.shared.validate.InputValidation;
import org.eclipse.scout.service.IService;

public interface IServerProcessingCancelService extends IService {

  /**
   * Cancel only specific backend job transaction of the same server session
   * <p>
   * Whenever a ClientJob - that is doing a backend call - is cancelled, this is detected by the
   * InternalHttpServiceTunnel.tunnelOnline and calls {@link IServerProcessingCancelService#cancel(long)}. The
   * server-side ServerProcessingCancelService calls ActiveTransactionRegistry.cancel
   * <p>
   * 
   * @return true if cancel was successful and transaction was in fact cancelled, false otherwise
   */
  @InputValidation(IValidationStrategy.NO_CHECK.class)
  boolean cancel(long requestSequence);
}
