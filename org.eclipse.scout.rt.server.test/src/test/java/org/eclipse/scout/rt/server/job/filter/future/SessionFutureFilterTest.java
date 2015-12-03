/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.job.filter.future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.junit.Test;

public class SessionFutureFilterTest {

  @Test
  public void test() {
    IServerSession session1 = mock(IServerSession.class);
    IServerSession session2 = mock(IServerSession.class);

    SessionFutureFilter filter = new SessionFutureFilter(session1);

    // Tests a Future of a job  without RunContext
    assertFalse(filter.accept(Jobs.schedule(mock(IRunnable.class), Jobs.newInput())));

    // Tests a Future of a job  with RunContext
    assertFalse(filter.accept(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(RunContexts.empty()))));

    // Tests a Future of a job  with ClientRunContext without session
    assertFalse(filter.accept(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ServerRunContexts.empty()))));

    // Tests a Future of a job  with ClientRunContext with correct session
    assertTrue(filter.accept(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ServerRunContexts.empty().withSession(session1)))));

    // Tests a Future of a job  with ClientRunContext with wrong session
    assertFalse(filter.accept(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ServerRunContexts.empty().withSession(session2)))));

    // Test adaptable to the session
    assertSame(session1, filter.getAdapter(ISession.class));
  }
}
