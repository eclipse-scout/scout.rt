/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoNode;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;
import org.junit.ComparisonFailure;

/**
 * Helper for unit tests dealing with {@link DoEntity}.
 */
@ApplicationScoped
public class DataObjectTestHelper {

  /**
   * Asserts (deep) equality for specified {@link DoEntity} objects and additionally asserts, that concrete
   * {@link DoEntity} class of expected entity and class of actual {@link DoEntity} is identical.
   */
  public void assertDoEntityEquals(IDoEntity expected, IDoEntity actual) {
    assertDoEntityEquals(expected, actual, true);
  }

  /**
   * Asserts (deep) equality for specified {@link DoEntity} objects.
   *
   * @param assertClassEquals
   *          if {@code true} concrete class of both {@link DoEntity} must be the identical
   */
  public void assertDoEntityEquals(IDoEntity expected, IDoEntity actual, boolean assertClassEquals) {
    if (!equalsObject(expected, actual, assertClassEquals)) {
      assertFail(expected, actual);
    }

    // assert all attribute names are set correctly to be equals to the corresponding map key
    assertMapKeyEqualsAttributeName(actual);
    assertMapKeyEqualsAttributeName(expected);
  }

  /**
   * Asserts (deep) equality for specified {@link DoList} objects and additionally asserts, that all nested concrete
   * {@link DoEntity} classes within the specified lists are identical.
   */
  public void assertDoListEquals(DoList<?> expected, DoList<?> actual) {
    assertDoListEquals(expected, actual, true);
  }

  /**
   * Asserts (deep) equality for specified {@link DoList} objects.
   *
   * @param assertClassEquals
   *          if {@code true} concrete class of all nested {@link DoEntity}'s must be the identical
   */
  public void assertDoListEquals(DoList<?> expected, DoList<?> actual, boolean assertClassEquals) {
    if (!equalsObject(expected, actual, assertClassEquals)) {
      assertFail(expected, actual);
    }
  }

  protected void assertMapKeyEqualsAttributeName(IDoEntity actual) {
    for (String key : actual.allNodes().keySet()) {
      assertEquals("key of attribute map is not equals to node attribute name", key, actual.getNode(key).getAttributeName());
    }
  }

  /**
   * Asserts (deep) equality of two {@link Object}, taking into account nested {@link DoNode} elements which requires
   * custom equality check.
   */
  public void assertObjectEquals(Object expected, Object actual, boolean assertClassEquals) {
    if (!equalsObject(expected, actual, assertClassEquals)) {
      assertFail(expected, actual);
    }
  }

  protected void assertFail(Object expected, Object actual) {
    throw new ComparisonFailure("Objects not equal", Objects.toString(expected), Objects.toString(actual));
  }

  /**
   * Check equality of two {@link Object}, taking into account nested {@link DoNode} elements which requires custom
   * equality check.
   * <p>
   * TODO [8.0] pbz: Add assert-param object instead of 'boolean assertClassEquals', e.g. allow BigDecimal == double for
   * raw DO's
   *
   * @return {@code true} if equal
   */
  public boolean equalsObject(Object expected, Object actual, boolean assertClassEquals) {
    if (expected == null || actual == null) {
      return expected == actual;
    }

    if (expected instanceof IDoEntity) {
      if (assertClassEquals && expected.getClass() != actual.getClass()) {
        return false;
      }
      return equalsObject(((IDoEntity) expected).allNodes(), ((IDoEntity) actual).allNodes(), assertClassEquals);
    }
    else if (expected instanceof DoNode) {
      return expected.getClass() == actual.getClass() && equalsObject(((DoNode<?>) expected).get(), ((DoNode<?>) actual).get(), assertClassEquals);
    }
    else if (expected instanceof Collection) {
      Collection<?> expectedCollection = (Collection<?>) expected;
      Collection<?> actualCollection = (Collection<?>) actual;
      if (expectedCollection.size() != actualCollection.size()) {
        return false;
      }
      if (expected instanceof List) {
        Iterator<?> expectedIter = expectedCollection.iterator();
        Iterator<?> actualIter = actualCollection.iterator();
        while (expectedIter.hasNext()) {
          if (!equalsObject(expectedIter.next(), actualIter.next(), assertClassEquals)) {
            return false;
          }
        }
        return true;
      }
      else {
        for (Object expectedElement : expectedCollection) {
          boolean found = false;
          Iterator<?> actualIter = actualCollection.iterator();
          while (actualIter.hasNext() && !found) {
            found = equalsObject(expectedElement, actualIter.next(), assertClassEquals);
          }
          if (!found) {
            return false;
          }
        }
        return true;
      }
    }
    else if (expected instanceof Map) {
      Map<?, ?> expectedMap = (Map<?, ?>) expected;
      Map<?, ?> actualMap = (Map<?, ?>) actual;
      if (expectedMap.size() != actualMap.size()) {
        return false;
      }

      for (Entry<?, ?> expectedEntry : expectedMap.entrySet()) {
        if (!equalsObject(expectedEntry.getValue(), actualMap.get(expectedEntry.getKey()), assertClassEquals)) {
          return false;
        }
      }
      return true;
    }
    else if (expected.getClass().isArray()) {
      return Objects.deepEquals(expected, actual);// delegates to Arrays.deepEquals0()
    }
    else {
      return expected.equals(actual);
    }
  }
}
