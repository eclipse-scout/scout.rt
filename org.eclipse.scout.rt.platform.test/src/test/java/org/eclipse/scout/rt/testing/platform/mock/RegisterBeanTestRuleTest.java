/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.mock;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(PlatformTestRunner.class)
public class RegisterBeanTestRuleTest {

  @Test
  public void testInvalidBeanClazz() {
    assertThrows(AssertionError.class, () -> new RegisterBeanTestRule<>(FixtureBean.class, Mockito.mock(FixtureBeanReplacement.class)).run(() -> {
      // nop
    }));
  }

  @Test
  public void testRegisterAndUnregisterBeanByInterface() {
    FixtureBeanReplacement objectWithoutRule = BEANS.get(FixtureBeanReplacement.class);
    assertEquals(FixtureBeanReplacement.class, objectWithoutRule.getClass());
    assertEquals(objectWithoutRule, BEANS.get(IFixtureBean.class));
    assertEquals(objectWithoutRule, BEANS.get(FixtureBean.class));

    FixtureBeanReplacement mock = Mockito.mock(FixtureBeanReplacement.class);
    new RegisterBeanTestRule<>(IFixtureBean.class, mock).run(() -> {
          assertEquals(mock, BEANS.get(IFixtureBean.class));
          assertEquals(objectWithoutRule, BEANS.get(FixtureBean.class));
          assertEquals(objectWithoutRule, BEANS.get(FixtureBeanReplacement.class));
        }
    );

    assertEquals(objectWithoutRule, BEANS.get(IFixtureBean.class));
    assertEquals(objectWithoutRule, BEANS.get(FixtureBean.class));
    assertEquals(objectWithoutRule, BEANS.get(FixtureBeanReplacement.class));
  }

  @Test
  public void testRegisterAndUnregisterBeanByClass() {
    FixtureBeanReplacement objectWithoutRule = BEANS.get(FixtureBeanReplacement.class);
    assertEquals(FixtureBeanReplacement.class, objectWithoutRule.getClass());
    assertEquals(objectWithoutRule, BEANS.get(IFixtureBean.class));
    assertEquals(objectWithoutRule, BEANS.get(FixtureBean.class));

    FixtureBeanReplacement mock = Mockito.mock(FixtureBeanReplacement.class);
    new RegisterBeanTestRule<>(FixtureBeanReplacement.class, mock).run(() -> {
          assertEquals(mock, BEANS.get(IFixtureBean.class));
          assertEquals(mock, BEANS.get(FixtureBean.class));
          assertEquals(mock, BEANS.get(FixtureBeanReplacement.class));
        }
    );

    assertEquals(objectWithoutRule, BEANS.get(IFixtureBean.class));
    assertEquals(objectWithoutRule, BEANS.get(FixtureBean.class));
    assertEquals(objectWithoutRule, BEANS.get(FixtureBeanReplacement.class));
  }

  @ApplicationScoped
  public interface IFixtureBean {
  }

  public static class FixtureBean implements IFixtureBean {
  }

  @Replace
  public static class FixtureBeanReplacement extends FixtureBean {
  }
}
