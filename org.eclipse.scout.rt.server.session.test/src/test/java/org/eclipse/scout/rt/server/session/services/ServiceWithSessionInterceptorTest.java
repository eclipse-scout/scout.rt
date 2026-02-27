/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session.services;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.server.session.TestServerSession;
import org.eclipse.scout.rt.server.session.context.ServerSessionRunContexts;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ServiceWithSessionInterceptorTest {

  private static TestServerSession s_serverSession;

  @BeforeClass
  public static void setUp() {
    s_serverSession = new TestServerSession();
    Platform.get().getBeanManager().registerClass(TestService.class);
  }

  @AfterClass
  public static void tearDown() {
    s_serverSession = null;
  }

  @Test
  public void testService() {
    ServerSessionRunContexts.empty().withSession(s_serverSession).run(() -> runInServerRunContext());
  }

  protected void runInServerRunContext() {
    BEANS.get(ITestService.class).doit();
  }

  public interface ITestService extends IService {

    void doit();
  }

  @ApplicationScoped
  public static class TestService implements ITestService {
    @Override
    public void doit() {

    }
  }
}
