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

import java.util.Collection;
import java.util.Map;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

/**
 * This data object represents a Scout JS object model and supports subclassing.<br>
 * <b>IMPORTANT</b>: Never add a {@link TypeVersion} and never persist this data object.
 */
@TypeName("scout.PermissionCollection")
public class PermissionCollectionDo extends DoEntity {

  /**
   * String used to identify the Scout JS object created by this object model.
   */
  public DoValue<String> objectType() {
    return doValue("objectType");
  }

  public DoValue<PermissionCollectionType> type() {
    return doValue("type");
  }

  public DoValue<Map<PermissionId, ? extends Collection<PermissionDo>>> permissions() {
    return doValue("permissions");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  /**
   * See {@link #objectType()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public PermissionCollectionDo withObjectType(String objectType) {
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
  public PermissionCollectionDo withType(PermissionCollectionType type) {
    type().set(type);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PermissionCollectionType getType() {
    return type().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PermissionCollectionDo withPermissions(Map<PermissionId, ? extends Collection<PermissionDo>> permissions) {
    permissions().set(permissions);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<PermissionId, ? extends Collection<PermissionDo>> getPermissions() {
    return permissions().get();
  }
}
