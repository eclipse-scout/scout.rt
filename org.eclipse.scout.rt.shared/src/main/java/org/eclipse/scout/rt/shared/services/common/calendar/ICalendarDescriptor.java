/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
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
   * Unique identifyer of a calendar. Calendar items reference this ID with the {@link ICalendarItem#getCalendarId()}
   * <br>
   * Required property, set via constructor
   */
  String getCalendarId();

  /**
   * Name of the Calendar E.g. Jeremy White <br>
   * Required property, set via constructor
   */
  String getName();

  ICalendarDescriptor withName(String name);

  /**
   * Unique identifyer of the parent calendar. <br>
   * Do not set this, if ths is a calendar group or a top level calendar.
   */
  String getParentId();

  ICalendarDescriptor withParentId(String parentId);

  /**
   * Indicates if the calendar is displayed
   */
  boolean isVisible();

  ICalendarDescriptor withVisible(boolean visible);

  /**
   * Indicates if the calendar can be selected via range selection
   */
  boolean isSelectable();

  ICalendarDescriptor withSelectable(boolean selectable);

  /**
   * Css class of the calendar
   */
  String getCssClass();

  ICalendarDescriptor withCssClass(String cssClass);

  /**
   * Order of calendar
   */
  long getOrder();

  ICalendarDescriptor withOrder(long order);
}
