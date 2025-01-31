/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.filter.future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
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

    Predicate<IFuture<?>> filter = new RunContextFutureFilter(RunContext.class);
    assertFalse(filter.test(future1));
    assertTrue(filter.test(future2));
    assertTrue(filter.test(future3));
    assertTrue(filter.test(future4));

    filter = new RunContextFutureFilter(P_RunContext1.class);
    assertFalse(filter.test(future1));
    assertFalse(filter.test(future2));
    assertTrue(filter.test(future3));
    assertFalse(filter.test(future4));
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
