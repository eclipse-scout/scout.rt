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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
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
    assertNull(input.getId());
    assertNull(input.getSubject());
    assertNull(input.getLocale());
    assertEquals(JobInput.INFINITE_EXPIRATION, input.getExpirationTimeMillis());
  }

  @Test
  public void testCopy() {
    JobInput input = JobInput.empty();
    input.getPropertyMap().put("A", "B");
    input.setName("name");
    input.setId("123");
    input.setSubject(new Subject());
    input.setLocale(Locale.CANADA_FRENCH);
    input.setExpirationTime(10, TimeUnit.MINUTES);

    JobInput copy = input.copy();

    assertNotSame(input.getContext(), copy.getContext());
    assertEquals(toSet(input.getPropertyMap().iterator()), toSet(copy.getPropertyMap().iterator()));
    assertEquals(input.getName(), copy.getName());
    assertEquals(input.getId(), copy.getId());
    assertSame(input.getSubject(), copy.getSubject());
    assertSame(input.getLocale(), copy.getLocale());
    assertEquals(TimeUnit.MINUTES.toMillis(10), copy.getExpirationTimeMillis());
  }

  @Test
  public void testDefaultName() {
    assertNull(JobInput.defaults().getName());
    assertEquals("ABC", JobInput.defaults().setName("ABC").getName());
  }

  @Test
  public void testDefaultId() {
    assertNull(JobInput.defaults().getId());
    assertEquals("123", JobInput.defaults().setId("123").getId());
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
    input.setSubject(null);
    assertNull(input.getSubject());
  }

  @Test
  public void testDefaultPropertyMap() {
    PropertyMap propertyMap = new PropertyMap();
    propertyMap.put("prop", "value");

    // No context on ThreadLocal
    PropertyMap.CURRENT.remove();
    assertNotNull(JobInput.defaults().getContext());

    // Context on ThreadLocal
    PropertyMap.CURRENT.set(propertyMap);
    assertNotSame(propertyMap, JobInput.defaults().getContext());
    assertEquals(toSet(propertyMap.iterator()), toSet(JobInput.defaults().getPropertyMap().iterator()));
  }

  @Test
  public void testDefaultLocale() {
    NlsLocale.CURRENT.remove();
    assertNull(JobInput.defaults().getLocale());

    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, JobInput.defaults().getLocale());

    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.KOREAN, JobInput.defaults().setLocale(Locale.KOREAN).getLocale());
  }

  private static Set<Object> toSet(Iterator<?> iterator) {
    Set<Object> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }
}
