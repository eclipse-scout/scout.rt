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
package org.eclipse.scout.commons.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import org.junit.Test;

public class JobInputTest {

  @Test
  public void testEmpty() {
    IJobInput input = JobInput.empty();
    assertNotNull(input.getContext());
    assertNull(input.getName());
    assertEquals(0, input.getId());
    assertNull(input.getSubject());
  }

  @Test
  public void testDefault1() {
    IJobInput input = JobInput.defaults();
    assertNotNull(input.getContext());
    assertNull(input.getName());
    assertEquals(0, input.getId());
    assertNull(input.getSubject());
  }

  @Test
  public void testDefault2() {
    Subject subject = new Subject();

    JobContext ctx = new JobContext();
    ctx.set("prop", "value");
    JobContext.CURRENT.set(new JobContext());

    IJobInput input = Subject.doAs(subject, new PrivilegedAction<IJobInput>() {

      @Override
      public IJobInput run() {
        return JobInput.defaults();
      }
    });

    assertNotSame(JobContext.CURRENT.get(), input.getContext());
    assertEquals(JobContext.CURRENT.get().iterator(), input.getContext().iterator());
    assertNull(input.getName());
    assertEquals(0, input.getId());
    assertSame(subject, input.getSubject());
  }

  @Test
  public void testIdentifier() {
    assertNull(JobInput.empty().getIdentifier(null));
    assertEquals("n/a", JobInput.empty().getIdentifier("n/a"));

    assertEquals("JOB", JobInput.empty().name("JOB").getIdentifier("n/a"));
    assertEquals("123", JobInput.empty().id(123).getIdentifier("n/a"));
    assertEquals("123;JOB", JobInput.empty().name("JOB").id(123).getIdentifier("n/a"));

    assertEquals("JOB", JobInput.empty().name("JOB").getIdentifier(null));
    assertEquals("123", JobInput.empty().id(123).getIdentifier(null));
    assertEquals("123;JOB", JobInput.empty().name("JOB").id(123).getIdentifier(null));

  }
}
