/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG-initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.date;

import java.util.Date;

import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.date.DateProvider;

/**
 * A date / time provider for testing whose methods return the same date and time when called repeatedly. This provider
 * needs to be registered <strong>manually</strong> with the {@link IBeanManager}.
 *
 * @since 15.0.0
 */
@IgnoreBean
public class FixedDateProvider extends DateProvider {

  private Date m_date;

  /**
   * Default constructor.<br/>
   * Will set the date to the current system time.
   */
  public FixedDateProvider() {
    this(new Date());
  }

  /**
   * Constructor to provide a date.
   *
   * @param date
   *          Date to return in subsequent invocations of the provider methods
   */
  public FixedDateProvider(Date date) {
    m_date = date;
  }

  /**
   * Retrieve the date/time of this provider
   *
   * @return the date/time returned by this provider
   */
  @Override
  public Date getDate() {
    return m_date;
  }

  /**
   * Change the date returned by the provider
   *
   * @param newDate
   *          new date to return as provider value
   */
  public void setDate(Date newDate) {
    m_date = newDate;
  }

}
