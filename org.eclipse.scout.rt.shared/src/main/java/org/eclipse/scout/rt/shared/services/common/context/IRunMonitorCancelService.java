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
package org.eclipse.scout.rt.shared.services.common.context;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.shared.TunnelToServer;

/**
 * Provides cancellation support for operations initiated by the client.
 */
@TunnelToServer
public interface IRunMonitorCancelService extends IService {

  /**
   * Use this constant to construct cancellation request.
   */
  String CANCEL_METHOD = "cancel";

  /**
   * Cancels all running operations which are associated with the given <code>requestSequence</code>. Technically, that
   * is any {@link ICancellable} which was bound to the {@link RunMonitor} of the originating service request and
   * includes {@link RunContext} executions, jobs and transactions.
   *
   * @param requestSequence
   *          <code>requestSequence</code> to identify the {@link RunMonitor} to cancel.
   * @return <code>true</code> if cancel was successful, <code>false</code> otherwise.
   */
  boolean cancel(long requestSequence);
}
