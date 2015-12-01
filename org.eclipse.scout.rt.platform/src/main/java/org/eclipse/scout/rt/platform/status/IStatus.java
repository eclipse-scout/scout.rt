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
package org.eclipse.scout.rt.platform.status;

/**
 * A status object represents the outcome of an operation. A status carries the following information:
 * <ul>
 * <li>severity (required)</li>
 * <li>message (required) - localized to current locale</li>
 * </ul>
 * Status bit masks are spaced such that new status can be added in between
 * <p>
 * Some status objects, known as multi-statuses, have other status objects as children.
 * </p>
 */
public interface IStatus extends Comparable<IStatus> {

  /**
   * no error
   */
  int OK = 0x01;

  /**
   * Status type severity (bit mask, 2^8) indicating this status is informational only.
   *
   * @see #getSeverity()
   * @see #matches(int)
   */
  int INFO = 0x100;

  /**
   * Status type severity (bit mask, 2^16) indicating this status represents a warning.
   *
   * @see #getSeverity()
   * @see #matches(int)
   */
  int WARNING = 0x10000;

  /**
   * Status type severity (bit mask, 2^24) indicating this status represents an error.
   *
   * @see #getSeverity()
   * @see #matches(int)
   */
  int ERROR = 0x1000000;

  /**
   * Returns the severity. The default severities are as follows (in descending order):
   * <ul>
   * <li><code>ERROR</code> - a serious error</li>
   * <li><code>WARNING</code> - a warning</li>
   * <li><code>INFO</code> - an informational message</li>
   * </ul>
   */
  int getSeverity();

  /**
   * Returns whether the severity of this status matches the given severity mask.
   *
   * @param severityMask
   *          a mask formed by bitwise or'ing severity mask constants (<code>ERROR</code>, <code>WARNING</code>,
   *          <code>INFO</code>, <code>CANCEL</code>)
   * @return <code>true</code> if there is at least one match, <code>false</code> if there are no matches
   * @see #getSeverity()
   * @see #ERROR
   * @see #WARNING
   * @see #INFO
   */
  boolean matches(int severityMask);

  /**
   * Returns the localized message describing the outcome. Not <code>null</code>
   *
   * @return a localized message
   */
  String getMessage();

  /**
   * Returns whether this status is a multi-status. A multi-status describes the outcome of an operation involving
   * multiple operands.
   * <p>
   * The severity of a multi-status is derived from the severities of its children.
   * </p>
   *
   * @return <code>true</code> for a multi-status, <code>false</code> otherwise
   */
  boolean isMultiStatus();

  /**
   * @return error code
   */
  int getCode();

  /**
   * @return true, if the severity is #OK
   */
  boolean isOK();

  /**
   * @return Returns the object's order. Statuses are ordered first by severity then by priority.
   */
  double getOrder();

}
