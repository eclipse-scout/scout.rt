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
package org.eclipse.scout.rt.client.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.junit.Test;

public class ClientJobInputTest {

  @Test
  public void testEmpty() {
    ClientJobInput input = ClientJobInput.empty();
    assertNotNull(input.getContext());
    assertNull(input.getName());
    assertEquals(0, input.getId());
    assertNull(input.getSubject());
    assertNull(input.getSession());
  }

  /**
   * Tests defaults for ClientJobInput in empty calling context.
   */
  @Test
  public void testDefault1() {
    ClientJobInput input = ClientJobInput.defaults();
    assertNotNull(input.getContext());
    assertNull(input.getName());
    assertEquals(0, input.getId());
    assertNull(input.getSubject());
    assertNull(input.getSession());
  }

  /**
   * Tests defaults for ClientJobInput in valid calling context.
   */
  @Test
  public void testDefault2() {
    ISession.CURRENT.set(mock(IClientSession.class));
    Subject subject = new Subject();

    JobContext ctx = new JobContext();
    ctx.set("prop", "value");
    JobContext.CURRENT.set(new JobContext());

    try {
      ClientJobInput input = Subject.doAs(subject, new PrivilegedAction<ClientJobInput>() {

        @Override
        public ClientJobInput run() {
          return ClientJobInput.defaults();
        }
      });

      assertNotSame(JobContext.CURRENT.get(), input.getContext());
      assertEquals(JobContext.CURRENT.get().iterator(), input.getContext().iterator());
      assertNull(input.getName());
      assertEquals(0, input.getId());
      assertSame(subject, input.getSubject());
      assertSame(ISession.CURRENT.get(), input.getSession());
    }
    finally {
      ISession.CURRENT.remove();
    }
  }
}
