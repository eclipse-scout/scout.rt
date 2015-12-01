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
package org.eclipse.scout.rt.platform.job.filter.future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class RunContextFutureFilterTest {

  @Test
  public void test() {
    IFuture<Void> future1 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput());
    IFuture<Void> future2 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(RunContexts.empty()));
    IFuture<Void> future3 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(new P_RunContext1()));
    IFuture<Void> future4 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(new P_RunContext2()));

    IFilter<IFuture<?>> filter = new RunContextFutureFilter(RunContext.class);
    assertFalse(filter.accept(future1));
    assertTrue(filter.accept(future2));
    assertTrue(filter.accept(future3));
    assertTrue(filter.accept(future4));

    filter = new RunContextFutureFilter(P_RunContext1.class);
    assertFalse(filter.accept(future1));
    assertFalse(filter.accept(future2));
    assertTrue(filter.accept(future3));
    assertFalse(filter.accept(future4));
  }

  private static class P_RunContext1 extends RunContext {

    @Override
    public RunContext copy() {
      final P_RunContext1 copy = new P_RunContext1();
      copy.copyValues(this);
      return copy;
    }
  }

  private static class P_RunContext2 extends RunContext {

    @Override
    public RunContext copy() {
      final P_RunContext2 copy = new P_RunContext2();
      copy.copyValues(this);
      return copy;
    }
  }
}
