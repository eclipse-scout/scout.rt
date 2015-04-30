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
package org.eclipse.scout.rt.platform.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.internal.fixture.DirectlyAnnotatedBean;
import org.eclipse.scout.rt.platform.internal.fixture.IndirectlyAnnotatedByBeanAnnotatedAnnotationBean;
import org.eclipse.scout.rt.platform.internal.fixture.IndirectlyAnnotatedByInterfaceBean;
import org.eclipse.scout.rt.platform.internal.fixture.IndirectlyAnnotatedBySuperclassBean;
import org.eclipse.scout.rt.platform.internal.fixture.SuppressBeanBean;
import org.eclipse.scout.rt.platform.internal.fixture.SuppressBeanOnAnnotationBean;
import org.eclipse.scout.rt.platform.internal.fixture.SuppressBeanOnInterfaceBean;
import org.eclipse.scout.rt.platform.internal.fixture.SuppressBeanOnSuperclassBean;
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
  public void testSuppressBean() {
    assertNull(BEANS.opt(SuppressBeanBean.class));
  }

  @Test
  public void testSuppressBeanOnSuperclass() {
    assertNotNull(BEANS.opt(SuppressBeanOnSuperclassBean.class));
  }

  @Test
  public void testSuppressBeanOnInterface() {
    assertNotNull(BEANS.opt(SuppressBeanOnInterfaceBean.class));
  }

  @Test
  public void testSuppressBeanOnAnnotation() {
    assertNotNull(BEANS.opt(SuppressBeanOnAnnotationBean.class));
  }
}
