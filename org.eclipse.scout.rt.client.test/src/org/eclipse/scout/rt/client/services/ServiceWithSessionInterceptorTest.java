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
package org.eclipse.scout.rt.client.services;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.ClientJob;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;
import org.eclipse.scout.rt.platform.cdi.OBJ;
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

  private static TestEnvironmentClientSession clientSession;

  @BeforeClass
  public static void setUp() {
    clientSession = new TestEnvironmentClientSession();
    OBJ.register(TestService.class);
  }

  @AfterClass
  public static void tearDown() {
    clientSession = null;
  }

  @Test
  public void testService() throws Exception {
    ClientJob job = new ClientJob("testJob", clientSession) {
      @Override
      protected void run() throws Exception {
        runInClientJob();
      }
    };
    job.runNow();
  }

  /**
   *
   */
  protected void runInClientJob() {
    OBJ.NEW(TestService.class).doit();

  }

  public static interface ITestService extends IService {

    /**
     *
     */
    void doit();

  }

  @SessionRequired(IClientSession.class)
  @ApplicationScoped
  public static class TestService extends AbstractService implements ITestService {
    @Override
    public void doit() {

    }
  }
}
