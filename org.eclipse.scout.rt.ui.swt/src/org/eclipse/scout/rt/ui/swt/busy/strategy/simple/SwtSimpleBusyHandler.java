/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.busy.strategy.simple;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.busy.SwtBusyHandler;

/**
 * Busy handler which only shows the busy indicator.
 * 
 * @author imo
 * @since 3.8
 */
public class SwtSimpleBusyHandler extends SwtBusyHandler {
  private boolean m_abortable = false;

  public SwtSimpleBusyHandler(IClientSession session, ISwtEnvironment env) {
    this(session, env, true);
  }

  public SwtSimpleBusyHandler(IClientSession session, ISwtEnvironment env, boolean abortable) {
    super(session, env);
    m_abortable = abortable;
  }

  @Override
  protected void runBusy(Job job) {
    ShowBusyJob busyWaitJob = new ShowBusyJob(TEXTS.get("BusyJob"), this);
    busyWaitJob.setSystem(!m_abortable);
    busyWaitJob.schedule();
  }

}
