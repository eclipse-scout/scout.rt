/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.job;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.Test;

/**
 * Test for JobEx
 */
public class JobExTest {

  @Test
  public void testSuccessfulRunNow() {
    JobEx j = new JobEx("testJob") {

      @Override
      protected IStatus run(IProgressMonitor monitor) {
        return Status.OK_STATUS;
      }
    };
    final IStatus res = j.runNow(null);
    assertEquals(Status.OK_STATUS, res);
    assertEquals(Status.OK_STATUS, j.getLastResult());
  }

  @Test
  public void testSuccessfulRunWithRunNow() throws InterruptedException {
    CancelJob j = new CancelJob("testJob");
    j.schedule();
    j.join();
    j.setCancel(true);
    final IStatus res = j.runNow(null);
    assertEquals(Status.CANCEL_STATUS, res);
    assertEquals(Status.CANCEL_STATUS, j.getLastResult());
  }

  @Test
  public void testSuccessfulRunNowThenRun() throws InterruptedException {
    CancelJob j = new CancelJob("testJob");
    j.runNow(null);
    j.setCancel(true);
    j.schedule();
    j.join();
    assertEquals(Status.CANCEL_STATUS, j.getResult());
    assertEquals(Status.CANCEL_STATUS, j.getLastResult());
  }

  @Test(expected = RuntimeException.class)
  public void testRunNowRuntimeException() {
    JobEx j = createRuntimeExceptionJob();
    j.runNow(null);
  }

  private JobEx createRuntimeExceptionJob() {
    return new JobEx("testJob") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        throw new RuntimeException();
      }
    };
  }

  class CancelJob extends JobEx {
    boolean cancel = false;

    public CancelJob(String name) {
      super(name);
    }

    public void setCancel(boolean cancel) {
      this.cancel = cancel;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      return (cancel) ? Status.CANCEL_STATUS : Status.OK_STATUS;
    }
  }

}
