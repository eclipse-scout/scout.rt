/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform;

import static org.junit.Assert.assertNotNull;

import org.eclipse.scout.rt.platform.exception.BeanCreationException;
import org.eclipse.scout.rt.platform.internal.fixture.BeanWithCircularConstructorDependency;
import org.eclipse.scout.rt.platform.internal.fixture.BeanWithCircularFieldDependency;
import org.eclipse.scout.rt.platform.internal.fixture.BeanWithEmptyInjection;
import org.eclipse.scout.rt.platform.internal.fixture.BeanWithInjectedField;
import org.eclipse.scout.rt.platform.internal.fixture.BeanWithInjections;
import org.eclipse.scout.rt.platform.internal.fixture.InjectionCascade;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for beans with injected fields and constructor arguments
 */
@RunWith(PlatformTestRunner.class)
public class InjectionTest {

  @Test
  public void testEmptyConstructor() {
    BeanWithEmptyInjection testBean = BEANS.get(BeanWithEmptyInjection.class);
    assertNotNull(testBean);
  }

  @Test
  public void testBeanWithInjections() {
    BeanWithInjections bean = BEANS.get(BeanWithInjections.class);
    bean.assertInit();
  }

  @Test
  public void testInjectionCascade() {
    InjectionCascade bean = BEANS.get(InjectionCascade.class);
    bean.assertInit();
  }

  @Test
  public void testInjectedField() {
    BeanWithInjectedField bean = BEANS.get(BeanWithInjectedField.class);
    bean.assertInit();
  }

  @Test(expected = BeanCreationException.class)
  public void testBeanWithCircularConstructorDependency() {
    BeanWithCircularConstructorDependency bean = BEANS.get(BeanWithCircularConstructorDependency.class);
    bean.assertInit();
  }

  @Test(expected = BeanCreationException.class)
  public void testBeanWithCircularFieldDependency() {
    BeanWithCircularFieldDependency bean = BEANS.get(BeanWithCircularFieldDependency.class);
    bean.assertInit();
  }
}
