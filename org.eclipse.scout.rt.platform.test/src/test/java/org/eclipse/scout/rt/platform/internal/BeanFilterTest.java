/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.internal;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.internal.fixture.DirectlyAnnotatedBean;
import org.eclipse.scout.rt.platform.internal.fixture.IgnoreBeanBean;
import org.eclipse.scout.rt.platform.internal.fixture.IgnoreBeanOnAnnotationBean;
import org.eclipse.scout.rt.platform.internal.fixture.IgnoreBeanOnInterfaceBean;
import org.eclipse.scout.rt.platform.internal.fixture.IgnoreBeanOnSuperclassBean;
import org.eclipse.scout.rt.platform.internal.fixture.IndirectlyAnnotatedByBeanAnnotatedAnnotationBean;
import org.eclipse.scout.rt.platform.internal.fixture.IndirectlyAnnotatedByInterfaceBean;
import org.eclipse.scout.rt.platform.internal.fixture.IndirectlyAnnotatedBySuperclassBean;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 5.1
 */
@RunWith(PlatformTestRunner.class)
public class BeanFilterTest {

  @Test
  public void testDirectlyAnnotated() {
    assertNotNull(BEANS.opt(DirectlyAnnotatedBean.class));
  }

  @Test
  public void testIndirectlyAnnotatedBySuperClass() {
    assertNotNull(BEANS.opt(IndirectlyAnnotatedBySuperclassBean.class));
  }

  @Test
  public void testIndirectlyAnnotatedByInterface() {
    assertNotNull(BEANS.opt(IndirectlyAnnotatedByInterfaceBean.class));
  }

  @Test
  public void testIndirectlyAnnotatedByBeanAnnotatedAnnotation() {
    assertNotNull(BEANS.opt(IndirectlyAnnotatedByBeanAnnotatedAnnotationBean.class));
  }

  @Test
  public void testIgnoreBean() {
    assertNull(BEANS.opt(IgnoreBeanBean.class));
  }

  @Test
  public void testIgnoreBeanOnSuperclass() {
    assertNotNull(BEANS.opt(IgnoreBeanOnSuperclassBean.class));
  }

  @Test
  public void testIgnoreBeanOnInterface() {
    assertNotNull(BEANS.opt(IgnoreBeanOnInterfaceBean.class));
  }

  @Test
  public void testIgnoreBeanOnAnnotation() {
    assertNotNull(BEANS.opt(IgnoreBeanOnAnnotationBean.class));
  }
}
