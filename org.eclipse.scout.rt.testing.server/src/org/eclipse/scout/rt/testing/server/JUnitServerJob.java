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
package org.eclipse.scout.rt.testing.server;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.testing.shared.ScoutJUnitPluginTestExecutor;

/**
 * Runs all @Test annotated methods in all classes and then exit
 * <p>
 * Normally this is called from within a server application in the start method <code><pre>
 *   public Object start(IApplicationContext context) throws Exception {
 *     logger.info("server initialized");
 *     //
 *     new JUnitServerJob().schedule();
 *     return EXIT_OK;
 *   }
 * </pre></code>
 */
public class JUnitServerJob extends Job {
  public JUnitServerJob() {
    super("JUnit Server Job");
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    ScoutJUnitPluginTestExecutor scoutJUnitPluginTestExecutor = new ScoutJUnitPluginTestExecutor();
    final int code = scoutJUnitPluginTestExecutor.runAllTests();
    System.exit(code);
    return Status.OK_STATUS;
  }
}
