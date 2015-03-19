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

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.job.ServerJobs;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.IService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ServiceWithSessionInterceptorTest {

  private static TestServerSession serverSession;

  @BeforeClass
  public static void setUp() {
    serverSession = new TestServerSession();
    Platform.get().getBeanContext().registerClass(TestService.class);
  }

  @AfterClass
  public static void tearDown() {
    serverSession = null;
  }

  @Test
  public void testService() throws Exception {
    ServerJobs.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        runInServerJob();
      }
    }, ServerJobInput.empty().name("test-job").session(serverSession));
  }

  protected void runInServerJob() {
    OBJ.get(ITestService.class).doit();

  }

  public static interface ITestService extends IService {

    void doit();

  }

  @ApplicationScoped
  public static class TestService extends AbstractService implements ITestService {
    @Override
    public void doit() {

    }
  }
}
