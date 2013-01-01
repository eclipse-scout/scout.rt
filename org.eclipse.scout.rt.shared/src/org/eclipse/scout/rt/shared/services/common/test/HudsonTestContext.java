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
package org.eclipse.scout.rt.shared.services.common.test;

/**
 * @deprecated Use Scout JUnit Testing Support: {@link org.eclipse.scout.testing.client.runner.ScoutClientTestRunner} or
 *             {@link org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner} to run Unit tests. *
 */
@Deprecated
@SuppressWarnings("deprecation")
public class HudsonTestContext extends BasicTestContext {

  public HudsonTestContext() {
  }

  // XXX create a hudson-readable junit xml result file
  @Override
  public void end() {
    super.end();
    System.out.println("TEST RESULTS");
    for (TestStatus s : getStatusList()) {
      System.out.println(" " + s.getProduct() + "\t" + s.getTitle() + "\t" + s.getSubTitle() + "\t" + TestStatus.getSeverityAsText(s.getSeverity()));
    }
    System.out.format("Summary: %d sucessful tests, %d errors, %d warnings\n", getSeverityCount(TestStatus.INFO), getSeverityCount(TestStatus.WARNING), getSeverityCount(TestStatus.ERROR) + getSeverityCount(TestStatus.FATAL));
  }

}
