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

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.dataobject.IDataObject;
import org.eclipse.scout.rt.testing.platform.util.ScoutAssert;
import org.junit.ComparisonFailure;

/**
 * Helper for unit tests dealing with {@link IDataObject}.
 *
 * @deprecated use {@link ScoutAssert#assertEqualsWithComparisonFailure(Object, Object)} or
 *             {@link ScoutAssert#assertEqualsWithComparisonFailure(String, Object, Object)} instead
 *             <p>
 *             FIXME [10.0] pbz: Remove after up-merge
 */
@Deprecated
@ApplicationScoped
public class DataObjectTestHelper {

  /**
   * Asserts that two objects are equal. If they are not, an {@link ComparisonFailure} is thrown with a string
   * representation of the given objects. If <code>expected</code> and <code>actual</code> are <code>null</code>, they
   * are considered equal.
   *
   * @param expected
   *          expected value
   * @param actual
   *          actual value
   */
  public void assertEquals(Object expected, Object actual) {
    ScoutAssert.assertEqualsWithComparisonFailure(null, expected, actual);
  }

  /**
   * Asserts that two objects are equal. If they are not, an {@link ComparisonFailure} is thrown with the given message
   * and a string representation of the given objects. If <code>expected</code> and <code>actual</code> are
   * <code>null</code>, they are considered equal.
   *
   * @param message
   *          the identifying message for the {@link ComparisonFailure} (<code>null</code> okay)
   * @param expected
   *          expected value
   * @param actual
   *          actual value
   */
  public void assertEquals(String message, Object expected, Object actual) {
    ScoutAssert.assertEqualsWithComparisonFailure(message, expected, actual);
  }
}
