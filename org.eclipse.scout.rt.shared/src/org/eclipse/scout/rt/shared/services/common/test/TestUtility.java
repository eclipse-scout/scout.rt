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

import java.util.Collection;

/**
 * @deprecated Use Scout JUnit Testing Support: {@link org.eclipse.scout.testing.client.runner.ScoutClientTestRunner} or
 *             {@link org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner} to run Unit tests.
 */
@Deprecated
@SuppressWarnings("deprecation")
public final class TestUtility {
  private TestUtility() {
  }

  public static void runTests(ITestContext ctx, Collection<ITest> tests) {
    runTests(ctx, tests.toArray(new ITest[tests.size()]));
  }

  public static void runTests(ITestContext ctx, ITest[] tests) {
    ctx.begin();
    for (ITest t : tests) {
      try {
        t.setTestContext(ctx);
        try {
          t.setUp();
          try {
            t.run();
          }
          catch (Throwable t2) {
            TestStatus s = new TestStatus(t.getProduct(), t.getTitle(), t.getSubTitle());
            s.setMessage("run failed");
            s.setException(t2);
            s.setSeverity(TestStatus.ERROR);
            ctx.addStatus(s);
          }
        }
        catch (Throwable t1) {
          // setUp failed
          TestStatus s = new TestStatus(t.getProduct(), t.getTitle(), t.getSubTitle());
          s.setMessage("setUp failed");
          s.setSeverity(TestStatus.FATAL);
          s.setException(t1);
          ctx.addStatus(s);
        }
        finally {
          try {
            t.tearDown();
          }
          catch (Throwable t3) {
            // setUp failed
            TestStatus s = new TestStatus(t.getProduct(), t.getTitle(), t.getSubTitle());
            s.setMessage("tearDown failed");
            s.setSeverity(TestStatus.FATAL);
            s.setException(t3);
            ctx.addStatus(s);
          }
        }
      }
      finally {
        t.setTestContext(null);
      }
    }
    ctx.end();
  }

}
