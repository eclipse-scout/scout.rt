/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util.date;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;

/**
 * A {@link IDateProvider} for testing that is initialized with the current {@link Date} and ticks one minute on every
 * invocation.<p>
 * NOTE: This provider needs to be registered <strong>manually</strong> with the {@link IBeanManager}.
 */
@IgnoreBean
public class LogicalTimeDateProvider extends FixedDateProvider {

  private final AtomicInteger m_counter = new AtomicInteger();

  public LogicalTimeDateProvider() {
    super();
  }

  public LogicalTimeDateProvider(Date date) {
    super(date);
  }

  @Override
  public Date getDate() {
    return DateUtility.addMinutes(super.getDate(), m_counter.getAndIncrement());
  }
}
