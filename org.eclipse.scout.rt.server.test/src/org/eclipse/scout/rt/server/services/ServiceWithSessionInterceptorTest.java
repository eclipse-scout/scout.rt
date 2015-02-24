/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.fixture.TestServerSession;
import org.eclipse.scout.rt.shared.services.cdi.SessionRequired;
import org.eclipse.scout.rt.testing.platform.ScoutPlatformTestRunner;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.IService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(ScoutPlatformTestRunner.class)
public class ServiceWithSessionInterceptorTest {

  private static TestServerSession serverSession;

  @BeforeClass
  public static void setUp() {
    serverSession = new TestServerSession();
    OBJ.register(TestService.class);
  }

  @AfterClass
  public static void tearDown() {
    serverSession = null;
  }

  @Test
  public void testService() throws InterruptedException {
    ServerJob job = new ServerJob("testJob", serverSession) {
      @Override
      protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
        runInServerJob();
        return Status.OK_STATUS;
      }
    };
    job.schedule();
    job.join();
  }

  /**
   *
   */
  protected void runInServerJob() {
    OBJ.NEW(ITestService.class).doit();

  }

  public static interface ITestService extends IService {

    /**
     *
     */
    void doit();

  }

  @SessionRequired(IServerSession.class)
  @ApplicationScoped
  public static class TestService extends AbstractService implements ITestService {
    @Override
    public void doit() {

    }
  }
}
