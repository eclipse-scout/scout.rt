/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.dataobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoNode;
import org.eclipse.scout.rt.platform.dataobject.DoValue;

/**
 * Helper for unit tests dealing with {@link DoEntity}.
 */
@ApplicationScoped
public class DataObjectTestHelper {

  /**
   * Asserts (deep) equality for specified {@link DoEntity} objects and additionally asserts, that concrete {@link DoEntity} class of
   * expected entity and class of actual {@link DoEntity} is identical.
   */
  public void assertDoEntityEquals(DoEntity expected, DoEntity actual) {
    assertDoEntityEquals(expected, actual, true);
  }

  /**
   * Asserts (deep) equality for specified {@link DoEntity} objects.
   *
   * @param if
   *          {@code true} concrete class of both {@link DoEntity} must be the identical
   */
  public void assertDoEntityEquals(DoEntity expected, DoEntity actual, boolean assertClassEquals) {
    if (assertClassEquals) {
      assertEquals(expected.getClass(), actual.getClass());
    }
    assertObjectEquals(expected.all(), actual.all(), assertClassEquals);

    // assert all attribute names are set correctly to be equals to the corresponding map key
    assertMapKeyEqualsAttributeName(actual);
    assertMapKeyEqualsAttributeName(expected);
  }

  protected void assertMapKeyEqualsAttributeName(DoEntity actual) {
    for (String key : actual.all().keySet()) {
      assertEquals("key of attribute map is not equals to node attribute name", key, actual.getNode(key).getAttributeName());
    }
  }

  /**
   * Asserts (deep) equality of two {@link Object}, taking into account nested {@link DoNode} elements which requires
   * custom equality check.
   */
  public void assertObjectEquals(Object expected, Object actual, boolean assertClassEquals) {
    if (expected instanceof DoEntity) {
      assertDoEntityEquals((DoEntity) expected, (DoEntity) actual, assertClassEquals);
    }
    else if (expected instanceof DoValue) {
      assertEquals(expected.getClass(), actual.getClass());
      assertObjectEquals(((DoValue<?>) expected).get(), ((DoValue<?>) actual).get(), assertClassEquals);
    }
    else if (expected instanceof DoList) {
      assertEquals(expected.getClass(), actual.getClass());
      assertObjectEquals(((DoList<?>) expected).get(), ((DoList<?>) actual).get(), assertClassEquals);
    }
    else if (expected instanceof Collection) {
      Collection<?> expectedCollection = (Collection<?>) expected;
      Collection<?> actualCollection = (Collection<?>) actual;
      assertEquals("size of collection does not match", expectedCollection.size(), actualCollection.size());
      Iterator<?> expectedIter = expectedCollection.iterator();
      Iterator<?> actualIter = actualCollection.iterator();
      while (expectedIter.hasNext()) {
        assertObjectEquals(expectedIter.next(), actualIter.next(), assertClassEquals);
      }
    }
    else if (expected instanceof Map) {
      Map<?, ?> expectedMap = (Map<?, ?>) expected;
      Map<?, ?> actualMap = (Map<?, ?>) actual;
      assertEquals("size of map does not match", expectedMap.size(), actualMap.size());

      for (Object expectedKey : expectedMap.keySet()) {
        assertTrue("actual map does not contain expected key " + expectedKey, actualMap.containsKey(expectedKey));
        assertObjectEquals(expectedMap.get(expectedKey), actualMap.get(expectedKey), assertClassEquals);
      }
      for (Object actualKey : actualMap.keySet()) {
        assertTrue("expected map does not contain actual key " + actualKey, expectedMap.containsKey(actualKey));
        assertObjectEquals(expectedMap.get(actualKey), actualMap.get(actualKey), assertClassEquals);
      }
    }
    else {
      assertEquals(expected, actual);
    }
  }
}
