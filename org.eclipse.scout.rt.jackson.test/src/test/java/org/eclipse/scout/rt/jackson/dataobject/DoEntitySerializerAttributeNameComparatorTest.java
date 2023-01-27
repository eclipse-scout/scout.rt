/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Test;

/**
 * Tests for {@link DoEntitySerializerAttributeNameComparator}.
 */
public class DoEntitySerializerAttributeNameComparatorTest {

  // DoEntitySerializerAttributeNameComparator

  @Test
  public void testCompare() {
    ScoutDataObjectModuleContext context = BEANS.get(ScoutDataObjectModule.class).getModuleContext();
    DoEntitySerializerAttributeNameComparator comparator = BEANS.get(DoEntitySerializerAttributeNameComparator.class).init(context);

    // Equality
    //noinspection EqualsWithItself
    assertEquals(0, comparator.compare("alfa", "alfa"));
    assertEquals(0, comparator.compare(context.getTypeAttributeName(), context.getTypeAttributeName()));
    assertEquals(0, comparator.compare(context.getTypeVersionAttributeName(), context.getTypeVersionAttributeName()));
    assertEquals(0, comparator.compare(context.getContributionsAttributeName(), context.getContributionsAttributeName()));

    // Non-special attribute names
    assertTrue(comparator.compare("alfa", "bravo") < 0);
    assertTrue(comparator.compare("bravo", "alfa") > 0);

    // Special attribute names
    assertTrue(comparator.compare(context.getTypeAttributeName(), context.getTypeVersionAttributeName()) < 0);
    assertTrue(comparator.compare(context.getTypeVersionAttributeName(), "alfa") < 0);
    assertTrue(comparator.compare("alfa", context.getContributionsAttributeName()) < 0);
  }

  @Test
  public void testInitMayBeCalledOnlyOnce() {
    ScoutDataObjectModuleContext context1 = BEANS.get(ScoutDataObjectModule.class).getModuleContext();
    ScoutDataObjectModuleContext context2 = BEANS.get(ScoutDataObjectModule.class).getModuleContext();
    DoEntitySerializerAttributeNameComparator comparator = BEANS.get(DoEntitySerializerAttributeNameComparator.class)
        .init(context1);
    assertThrows(AssertionException.class, () -> comparator.init(context2));
  }

  @Test
  public void testDifferentConfigurations() {
    ScoutDataObjectModuleContext contextWithDefaultConfig = BEANS.get(ScoutDataObjectModule.class).getModuleContext();
    ScoutDataObjectModuleContext contextWithCustomTypeAttributeName = BEANS.get(ScoutDataObjectModule.class).getModuleContext()
        .withTypeAttributeName("foo");

    DoEntitySerializerAttributeNameComparator comparatorForDefaultConfig = BEANS.get(DoEntitySerializerAttributeNameComparator.class)
        .init(contextWithDefaultConfig);
    DoEntitySerializerAttributeNameComparator comparatorForCustomTypeAttributeName = BEANS.get(DoEntitySerializerAttributeNameComparator.class)
        .init(contextWithCustomTypeAttributeName);

    assertTrue(comparatorForDefaultConfig.compare("bar", "foo") < 0);
    assertTrue(comparatorForCustomTypeAttributeName.compare("bar", "foo") > 0);
    assertTrue(comparatorForCustomTypeAttributeName.compare("bar", "fun") < 0);
  }
}
