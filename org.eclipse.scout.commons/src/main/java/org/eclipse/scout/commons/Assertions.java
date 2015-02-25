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
package org.eclipse.scout.commons;

/**
 * Helper class to ensure the application's assumptions about expected values.
 *
 * @since 5.1
 */
public final class Assertions {

  private Assertions() {
    // private constructor for utility classes.
  }

  /**
   * Asserts the given value not to be <code>null</code>.
   *
   * @param value
   *          the value to be tested.
   * @return the given value if not <code>null</code>.
   * @throws AssertionException
   *           if the given value is <code>null</code>.
   */
  public static <T> T assertNotNull(final T value) {
    return assertNotNull(value, "expected 'non-null' object but was 'null'");
  }

  /**
   * Asserts the given value not to be <code>null</code>.
   *
   * @param value
   *          the value to be tested.
   * @param msg
   *          message contained in the {@link AssertionException} in case of an assertion error.
   * @param msgArgs
   *          arguments to be used in the message.
   * @return the given value if not <code>null</code>.
   * @throws AssertionException
   *           if the given value is <code>null</code>.
   */
  public static <T> T assertNotNull(final T value, final String msg, final Object... msgArgs) {
    if (value == null) {
      fail(msg, msgArgs);
    }
    return value;
  }

  /**
   * Asserts the given value not to be <code>null</code> or <code>empty</code>.
   *
   * @param value
   *          the value to be tested.
   * @return the given value if not <code>null</code> or <code>empty</code>.
   * @throws AssertionException
   *           if the given value is <code>null</code> or <code>empty</code>.
   */
  public static String assertNotNullOrEmpty(final String value) {
    if (value == null) {
      fail("expected 'non-null' String but was 'null'.");
    }
    else if (value.isEmpty()) {
      fail("expected 'non-empty' String but was 'empty'.");
    }
    return value;
  }

  /**
   * Asserts the given value not to be <code>null</code> or <code>empty</code>.
   *
   * @param value
   *          the value to be tested.
   * @param msg
   *          message contained in the {@link AssertionException} in case of an assertion error.
   * @param msgArgs
   *          arguments to be used in the message.
   * @return the given value if not <code>null</code> or <code>empty</code>.
   * @throws AssertionException
   *           if the given value is <code>null</code> or <code>empty</code>.
   */
  public static String assertNotNullOrEmpty(final String value, final String msg, final Object... msgArgs) {
    if (value == null || value.isEmpty()) {
      fail(msg, msgArgs);
    }
    return value;
  }

  /**
   * Asserts the given value to be <code>true</code>.
   *
   * @param value
   *          the value to be tested.
   * @return always <code>true</code>.
   * @throws AssertionException
   *           if the given value is <code>false</code>.
   */
  public static boolean assertTrue(final boolean value) {
    return assertTrue(value, "expected 'true' but was 'false'.");
  }

  /**
   * Asserts the given value to be <code>true</code>.
   *
   * @param value
   *          the value to be tested.
   * @param msg
   *          message contained in the {@link AssertionException} in case of an assertion error.
   * @param msgArgs
   *          arguments to be used in the message.
   * @return always <code>true</code>.
   * @throws AssertionException
   *           if the given value is <code>false</code>.
   */
  public static boolean assertTrue(final boolean value, final String msg, final Object... msgArgs) {
    if (!value) {
      fail(msg, msgArgs);
    }
    return value;
  }

  /**
   * Asserts the given value to be <code>false</code>.
   *
   * @param value
   *          the value to be tested.
   * @return always <code>false</code>.
   * @throws AssertionException
   *           if the given value is <code>true</code>.
   */
  public static boolean assertFalse(final boolean value) {
    return assertFalse(value, "expected 'false' but was 'true'.");
  }

  /**
   * Asserts the given value to be <code>false</code>.
   *
   * @param value
   *          the value to be tested.
   * @param msg
   *          message contained in the {@link AssertionException} in case of an assertion error.
   * @param msgArgs
   *          arguments to be used in the message.
   * @return always <code>false</code>.
   * @throws AssertionException
   *           if the given value is <code>true</code>.
   */
  public static boolean assertFalse(final boolean value, final String msg, final Object... msgArgs) {
    if (value) {
      fail(msg, msgArgs);
    }
    return value;
  }

  /**
   * To always throw an assertion exception.
   *
   * @param msg
   *          the message describing the assertion.
   * @param msgArgs
   *          message arguments to be referenced in the given message by <code>%s</code>.
   */
  public static void fail(final String msg, final Object... msgArgs) {
    throw new AssertionException("Assertion error: %s", String.format(msg, msgArgs));
  }

  /**
   * Indicates an assertion error about the application's assumptions about expected values.
   */
  public static class AssertionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AssertionException(final String msg, final Object... msgArgs) {
      super(String.format(msg, msgArgs));
    }
  }
}
