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
package org.eclipse.scout.rt.platform.context;

import static org.junit.Assert.assertEquals;
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

import javax.security.auth.Subject;

import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class RunContextTest {

  @Before
  public void before() {
    NlsLocale.CURRENT.remove();
  }

  @Test
  public void testEmpty() {
    RunContext runContext = RunContexts.empty();
    assertNull(runContext.getSubject());
    assertNull(runContext.getLocale());
    assertTrue(toSet(runContext.getPropertyMap().iterator()).isEmpty());
  }

  @Test
  public void testCopy() {
    RunContext runContext = RunContexts.empty();
    runContext.getPropertyMap().put("A", "B");
    runContext.subject(new Subject());
    runContext.locale(Locale.CANADA_FRENCH);

    RunContext copy = runContext.copy();

    assertEquals(toSet(runContext.getPropertyMap().iterator()), toSet(copy.getPropertyMap().iterator()));
    assertSame(runContext.getSubject(), copy.getSubject());
    assertSame(runContext.getLocale(), copy.getLocale());
  }

  @Test
  public void testCurrentSubject() {
    Subject subject = new Subject();
    RunContext runContext = Subject.doAs(subject, new PrivilegedAction<RunContext>() {

      @Override
      public RunContext run() {
        return RunContexts.copyCurrent();
      }
    });
    assertSame(subject, runContext.getSubject());

    runContext = Subject.doAs(null, new PrivilegedAction<RunContext>() {

      @Override
      public RunContext run() {
        return RunContexts.copyCurrent();
      }
    });
    assertNull(runContext.getSubject());
  }

  @Test
  public void testCurrentPropertyMap() {
    PropertyMap propertyMap = new PropertyMap();
    propertyMap.put("prop", "value");

    // No context on ThreadLocal
    PropertyMap.CURRENT.remove();
    assertNotNull(RunContexts.copyCurrent());
    assertTrue(toSet(RunContexts.copyCurrent().getPropertyMap().iterator()).isEmpty());

    // Context on ThreadLocal
    PropertyMap.CURRENT.set(propertyMap);
    assertNotSame(propertyMap, RunContexts.copyCurrent().getPropertyMap()); // test for copy
    assertEquals(toSet(propertyMap.iterator()), toSet(RunContexts.copyCurrent().getPropertyMap().iterator()));
  }

  @Test
  public void testCurrentLocale() {
    NlsLocale.CURRENT.remove();
    assertNull(RunContexts.copyCurrent().getLocale());

    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, RunContexts.copyCurrent().getLocale());

    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.KOREAN, RunContexts.copyCurrent().locale(Locale.KOREAN).getLocale());
  }

  private static Set<Object> toSet(Iterator<?> iterator) {
    Set<Object> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }
}
