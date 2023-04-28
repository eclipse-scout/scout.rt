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

import java.util.UUID;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_24_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

/**
 * Used to enable support for multiple calendars in the Calendar widget.
 * Describes a resource, like a person or a room.
 */
@TypeName("scout.CalendarResource")
@TypeVersion(Scout_24_2_001.class)
public class CalendarResourceDo extends DoEntity {

  public CalendarResourceDo() {
    withResourceId(UUID.randomUUID().toString());
    withVisible(true);
    withSelectable(true);
  }

  /**
   * Unique identifyer of a calendar resource. Calendar items reference this ID with the {@link ICalendarItem#getResourceId()}
   * <br>
   * Required property
   */
  public DoValue<String> resourceId() {
    return doValue("resourceId");
  }

  /**
   * Name of the resource e.g. Jeremy White
   */
  public DoValue<String> name() {
    return doValue("name");
  }

  /**
   * Unique identifyer of the parent resource. <br>
   * Do not set this, if ths is a calendar group or a top level calendar.
   */
  public DoValue<String> parentId() {
    return doValue("parentId");
  }

  /**
   * Indicates if the resource is displayed
   */
  public DoValue<Boolean> visible() {
    return doValue("visible");
  }

  /**
   * Indicates if the resource can be selected via range selection
   */
  public DoValue<Boolean> selectable() {
    return doValue("selectable");
  }

  /**
   * Css class of the resource
   */
  public DoValue<String> cssClass() {
    return doValue("cssClass");
  }

  /**
   * Order of calendar
   */
  public DoValue<Long> order() {
    return doValue("order");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  /**
   * See {@link #resourceId()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public CalendarResourceDo withResourceId(String resourceId) {
    resourceId().set(resourceId);
    return this;
  }

  /**
   * See {@link #resourceId()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public String getResourceId() {
    return resourceId().get();
  }

  /**
   * See {@link #name()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public CalendarResourceDo withName(String name) {
    name().set(name);
    return this;
  }

  /**
   * See {@link #name()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public String getName() {
    return name().get();
  }

  /**
   * See {@link #parentId()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public CalendarResourceDo withParentId(String parentId) {
    parentId().set(parentId);
    return this;
  }

  /**
   * See {@link #parentId()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public String getParentId() {
    return parentId().get();
  }

  /**
   * See {@link #visible()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public CalendarResourceDo withVisible(Boolean visible) {
    visible().set(visible);
    return this;
  }

  /**
   * See {@link #visible()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getVisible() {
    return visible().get();
  }

  /**
   * See {@link #visible()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public boolean isVisible() {
    return nvl(getVisible());
  }

  /**
   * See {@link #selectable()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public CalendarResourceDo withSelectable(Boolean selectable) {
    selectable().set(selectable);
    return this;
  }

  /**
   * See {@link #selectable()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getSelectable() {
    return selectable().get();
  }

  /**
   * See {@link #selectable()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public boolean isSelectable() {
    return nvl(getSelectable());
  }

  /**
   * See {@link #cssClass()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public CalendarResourceDo withCssClass(String cssClass) {
    cssClass().set(cssClass);
    return this;
  }

  /**
   * See {@link #cssClass()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public String getCssClass() {
    return cssClass().get();
  }

  /**
   * See {@link #order()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public CalendarResourceDo withOrder(Long order) {
    order().set(order);
    return this;
  }

  /**
   * See {@link #order()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Long getOrder() {
    return order().get();
  }
}
