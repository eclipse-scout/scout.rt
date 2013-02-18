/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.shared;

import java.util.Collection;

import org.eclipse.scout.commons.job.JobEx;

/**
 * Depreciated: use {@link org.eclipse.scout.rt.testing.commons.ScoutAssert} instead.
 * will be removed with the L-Release.
 */
@Deprecated
public final class ScoutAssert {

  private ScoutAssert() {
  }

  public static <T> void assertSetEquals(T[] expected, Collection<T> actual) {
    org.eclipse.scout.rt.testing.commons.ScoutAssert.assertSetEquals(expected, actual);
  }

  public static <T> void assertSetEquals(Collection<T> expected, Collection<T> actual) {
    org.eclipse.scout.rt.testing.commons.ScoutAssert.assertSetEquals(expected, actual);
  }

  public static <T> void assertListEquals(T[] expected, Collection<T> actual) {
    org.eclipse.scout.rt.testing.commons.ScoutAssert.assertListEquals(expected, actual);
  }

  public static <T> void assertListEquals(Collection<T> expected, Collection<T> actual) {
    org.eclipse.scout.rt.testing.commons.ScoutAssert.assertListEquals(expected, actual);
  }

  public static void assertOrder(Object[] expected, Object[] actual) {
    org.eclipse.scout.rt.testing.commons.ScoutAssert.assertOrder(expected, actual);
  }

  public static void assertOrder(String message, Object[] expected, Object[] actual) {
    org.eclipse.scout.rt.testing.commons.ScoutAssert.assertOrder(message, expected, actual);
  }

  public static void jobSuccessfullyCompleted(JobEx job) throws Throwable {
    org.eclipse.scout.rt.testing.commons.ScoutAssert.jobSuccessfullyCompleted(job);
  }

}
