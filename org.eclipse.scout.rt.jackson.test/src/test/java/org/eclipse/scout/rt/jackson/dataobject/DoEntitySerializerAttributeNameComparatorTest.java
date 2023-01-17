/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.BEANS;
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
}
