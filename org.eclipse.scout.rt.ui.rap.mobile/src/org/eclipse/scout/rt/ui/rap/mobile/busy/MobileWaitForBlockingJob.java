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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.rt.ui.rap.busy.RwtBusyHandler;
import org.eclipse.scout.rt.ui.rap.busy.WaitForBlockingJob;

public class MobileWaitForBlockingJob extends WaitForBlockingJob {

  public MobileWaitForBlockingJob(String name, RwtBusyHandler handler) {
    super(name, handler);
  }

  @Override
  protected void runBlocking(IProgressMonitor monitor) {
    //schedule blocking job
    new BusyBlockDialogJob(getName(), getBusyHandler()).schedule();
  }

}
