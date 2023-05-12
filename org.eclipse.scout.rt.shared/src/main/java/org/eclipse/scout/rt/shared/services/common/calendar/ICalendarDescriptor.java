/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.calendar;

/**
 * The ICalendarDescriptor is a descriptor object for a calendar. When you want to have multiple calendars, you need to
 * describe your calendar using this descriptor.
 */
public interface ICalendarDescriptor {

  /**
   * Unique identifyer of a calendar. Calendar items reference this id with the {@link ICalendarItem#getCalendarId()}
   */
  long getCalendarId();

  void setCalendarId(long id);

  /**
   * Name of the Calendar E.g. Jeremy White
   */
  String getName();

  void setName(String name);

  /**
   * Indicates if the calendar is displayed
   */
  boolean isVisible();

  void setVisible(boolean visible);

  /**
   * Css class of the calendar
   */
  String getCssClass();

  void setCssClass(String cssClass);
}
