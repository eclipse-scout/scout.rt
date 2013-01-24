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
package org.eclipse.scout.rt.ui.swt.form.fields.datefield.chooser;

/**
 * Class DateEquality. Since there are currently three states of equalities,
 * there is a state pattern needed.
 * 
 * @version 1.0
 * @since 2005
 */
public final class DateEquality {
  /**
   * The constant <code>NOT_EQUAL</code>
   */
  public static final DateEquality NOT_EQUAL = new DateEquality("NOT_EQUAL");

  /**
   * The constant <code>HOUR_EQUAL</code> the date matches to the hour.
   * <p>
   * 
   * <pre>
   *     12:00 - 12:59 matches to 12:00
   *     13:00 - 13:59 matches to 13:00
   *     ...
   * </pre>
   */
  public static final DateEquality HOUR_EQUAL = new DateEquality("HOUR_EQUAL");

  /**
   * The constant <code>QUARTER_EQUAL</code> the date matches in quarters.
   * <p>
   * 
   * <pre>
   *     12:00 - 12:14 matches to 12:00
   *     12:15 - 12:29 matches to 12:15
   *     ...
   * </pre>
   */
  public static final DateEquality QUARTER_EQUAL = new DateEquality("QUARTER_EQUAL");

  private String m_name = null;

  private DateEquality(String s) {
    m_name = s;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return m_name;
  }
}
