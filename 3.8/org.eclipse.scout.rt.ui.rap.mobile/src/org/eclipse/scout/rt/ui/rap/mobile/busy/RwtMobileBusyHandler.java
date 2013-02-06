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
package org.eclipse.scout.rt.ui.rap.mobile.busy;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.busy.RwtBusyHandler;

/**
 * Displays a waiting dialog on blocking instead of the blocking header.
 * 
 * @see {@link BusyBlockDialog}
 * @since 3.9.0
 */
public class RwtMobileBusyHandler extends RwtBusyHandler {

  public RwtMobileBusyHandler(IClientSession session, IRwtEnvironment env) {
    super(session, env);
  }

  @Override
  protected void runBusy(Job job) {
    new MobileWaitForBlockingJob(TEXTS.get("BusyJob"), this).schedule();
  }

}
