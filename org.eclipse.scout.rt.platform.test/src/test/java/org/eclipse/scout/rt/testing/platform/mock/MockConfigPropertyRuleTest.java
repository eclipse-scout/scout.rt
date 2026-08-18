/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.mock;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Testcase for {@link MockConfigPropertyRule}
 */
@RunWith(PlatformTestRunner.class)
public class MockConfigPropertyRuleTest {

  @Test
  public void testRule() {
    assertNull(CONFIG.getPropertyValue(FixtureProperty.class));
    assertFalse(Mockito.mockingDetails(BEANS.get(FixtureProperty.class)).isMock());

    new MockConfigPropertyRule<>(FixtureProperty.class, "mockValue").run(() -> {
      assertTrue(Mockito.mockingDetails(BEANS.get(FixtureProperty.class)).isMock());
      assertEquals("mockValue", CONFIG.getPropertyValue(FixtureProperty.class));
    });

    assertNull(CONFIG.getPropertyValue(FixtureProperty.class));
    assertFalse(Mockito.mockingDetails(BEANS.get(FixtureProperty.class)).isMock());
  }

  public static class FixtureProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.fixture.property description";
    }

    @Override
    public String description() {
      return "fixture property";
    }
  }
}
