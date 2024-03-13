/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.security;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

/**
 * This data object represents a Scout JS object model and supports subclassing.<br>
 * <b>IMPORTANT</b>: Never add a {@link TypeVersion} and never persist this data object.
 */
@TypeName("scout.Permission")
public class PermissionDo extends DoEntity {

  /**
   * String used to identify the Scout JS object created by this object model.
   */
  public DoValue<String> objectType() {
    return doValue("objectType");
  }

  public DoValue<PermissionId> id() {
    return doValue("id");
  }

  public DoValue<Integer> level() {
    return doValue("level");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  /**
   * See {@link #objectType()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public PermissionDo withObjectType(String objectType) {
    objectType().set(objectType);
    return this;
  }

  /**
   * See {@link #objectType()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public String getObjectType() {
    return objectType().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PermissionDo withId(PermissionId id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PermissionId getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PermissionDo withLevel(Integer level) {
    level().set(level);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getLevel() {
    return level().get();
  }
}
