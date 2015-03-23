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
package org.eclipse.scout.rt.ui.swing.ext.busy;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.client.busy.AbstractBusyHandler;
import org.eclipse.scout.rt.client.busy.IBusyHandler;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.job.ClientJobs;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * Swing default implementation of busy handling.
 * <p>
 * Show wait cursor as long as busy.
 * <p>
 * Block application after {@link IBusyHandler#getLongOperationMillis()}
 *
 * @author imo
 * @since 3.8
 */
public class SwingBusyHandler extends AbstractBusyHandler {

  public SwingBusyHandler() {
    super();
  }

  @Override
  protected void runBusy(IFuture<?> future) {
    IRunnable runnable = new SwingBusyJob(this);
    ClientJobs.schedule(runnable, ClientJobInput.fillCurrent().sessionRequired(false).name(TEXTS.get("BusyJob")));
  }
}
