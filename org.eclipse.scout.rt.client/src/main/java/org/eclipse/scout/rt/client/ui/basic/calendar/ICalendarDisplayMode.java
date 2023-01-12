/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.calendar;

/**
 * Interface providing display-modes for calendar.
 */
public interface ICalendarDisplayMode {

  // never change final constants (properties files might have references)
  // these constants should correspond with those from the planner
  int DAY = 1;
  int WEEK = 2;
  int MONTH = 3;
  int WORK_WEEK = 4;
}
