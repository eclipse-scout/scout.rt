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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.nls.NlsLocale;
import org.junit.Before;
import org.junit.Test;

public class JobInputTest {

  @Before
  public void before() {
    NlsLocale.CURRENT.remove();
  }

  @Test
  public void testEmpty() {
    JobInput input = JobInput.empty();
    assertNotNull(input.getContext());
    assertNull(input.getName());
    assertEquals(0, input.getId());
    assertNull(input.getSubject());
    assertNull(input.getLocale());
    assertEquals(IJobInput.INFINITE_EXPIRATION, input.getExpirationTimeMillis());
  }

  @Test
  public void testCopy() {
    JobInput input = JobInput.empty();
    input.getContext().set("A", "B");
    input.name("name");
    input.id(123);
    input.subject(new Subject());
    input.locale(Locale.CANADA_FRENCH);
    input.expirationTime(10, TimeUnit.MINUTES);

    JobInput copy = input.copy();

    assertNotSame(input.getContext(), copy.getContext());
    assertEquals(toSet(input.getContext().iterator()), toSet(copy.getContext().iterator()));
    assertEquals(input.getName(), copy.getName());
    assertEquals(input.getId(), copy.getId());
    assertSame(input.getSubject(), copy.getSubject());
    assertSame(input.getLocale(), copy.getLocale());
    assertEquals(TimeUnit.MINUTES.toMillis(10), copy.getExpirationTimeMillis());
  }

  @Test
  public void testDefaultName() {
    assertNull(JobInput.defaults().getName());
    assertEquals("ABC", JobInput.defaults().name("ABC").getName());
  }

  @Test
  public void testDefaultId() {
    assertEquals(0, JobInput.defaults().getId());
    assertEquals(123, JobInput.defaults().id(123).getId());
  }

  @Test
  public void testDefaultSubject() {
    assertNull(JobInput.defaults().getSubject());

    Subject subject = new Subject();
    JobInput input = Subject.doAs(subject, new PrivilegedAction<JobInput>() {

      @Override
      public JobInput run() {
        return JobInput.defaults();
      }
    });
    assertSame(subject, input.getSubject());

    subject = new Subject();
    input = Subject.doAs(subject, new PrivilegedAction<JobInput>() {

      @Override
      public JobInput run() {
        return JobInput.defaults();
      }
    });
    input.subject(null);
    assertNull(input.getSubject());
  }

  @Test
  public void testDefaultJobContext() {
    JobContext threadLocalContext = new JobContext();
    threadLocalContext.set("prop", "value");

    // No context on ThreadLocal
    JobContext.CURRENT.remove();
    assertNotNull(JobInput.defaults().getContext());

    // Context on ThreadLocal
    JobContext.CURRENT.set(threadLocalContext);
    assertNotSame(threadLocalContext, JobInput.defaults().getContext());
    assertEquals(toSet(threadLocalContext.iterator()), toSet(JobInput.defaults().getContext().iterator()));

    // Session on ThreadLocal, but set explicitly
    JobContext.CURRENT.set(threadLocalContext);
    JobContext explicitContext = new JobContext();
    assertSame(explicitContext, JobInput.defaults().context(explicitContext).getContext());
    assertTrue(toSet(JobInput.defaults().context(explicitContext).getContext().iterator()).isEmpty());

    // Context on ThreadLocal, but set explicity to null
    JobContext.CURRENT.set(threadLocalContext);
    assertNotNull(JobInput.defaults().context(null).getContext());
    assertNotEquals(threadLocalContext, JobInput.defaults().context(null).getContext());
    assertTrue(toSet(JobInput.defaults().context(null).getContext().iterator()).isEmpty());
  }

  @Test
  public void testDefaultLocale() {
    NlsLocale.CURRENT.remove();
    assertNull(JobInput.defaults().getLocale());

    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, JobInput.defaults().getLocale());

    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.KOREAN, JobInput.defaults().locale(Locale.KOREAN).getLocale());
  }

  private static Set<Object> toSet(Iterator<?> iterator) {
    Set<Object> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }
}
