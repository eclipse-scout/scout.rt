/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.testing.ui.rap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.testing.client.IGuiMockService;
import org.eclipse.scout.rt.testing.client.TestingClientSessionRegistryService;
import org.eclipse.scout.rt.testing.shared.ScoutJUnitPluginTestExecutor;
import org.eclipse.scout.service.SERVICES;

/**
 * Runs all @Test annotated methods in all classes and then exit
 * <p>
 * Normally this is called from within the implementing bundle activator in the start method <code><pre>
 *   public void start(BundleContext context) throws Exception {
 *     super.start(context);
 *     plugin = this;
 *     new JUnitRAPJob(ClientSession.class).schedule(200);
 *   }
 * </pre></code>
 */
public class JUnitRAPJob extends Job {
  private final Class<? extends IClientSession> m_clientSessionClass;

  public JUnitRAPJob(Class<? extends IClientSession> clientSessionClass) {
    super("JUnit RAP Job");
    setSystem(true);
    m_clientSessionClass = clientSessionClass;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    TestingClientSessionRegistryService testingClientSessionRegistryService = null;
    try {
      testingClientSessionRegistryService = TestingClientSessionRegistryService.registerTestingClientSessionRegistryService();
      //
      ScoutJUnitPluginTestExecutor scoutJUnitPluginTestExecutor = new ScoutJUnitPluginTestExecutor();
      final int code = scoutJUnitPluginTestExecutor.runAllTests();
      ((RapMockService) SERVICES.getService(IGuiMockService.class)).disposeServices();
      System.exit(code);
      return Status.OK_STATUS;
    }
    finally {
      TestingClientSessionRegistryService.unregisterTestingClientSessionRegistryService(testingClientSessionRegistryService);
    }
  }
}
