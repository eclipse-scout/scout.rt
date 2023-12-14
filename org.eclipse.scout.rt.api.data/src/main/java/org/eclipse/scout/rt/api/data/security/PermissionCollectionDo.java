/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.mapping.ToDoFunctionHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.security.AllPermissionCollection;
import org.eclipse.scout.rt.security.IPermissionCollection;
import org.eclipse.scout.rt.security.NonePermissionCollection;

/**
 * This data object represents a Scout JS object model and supports subclassing.<br>
 * <b>IMPORTANT</b>: Never add a {@link TypeVersion} and never persist this data object.
 */
@TypeName("scout.PermissionCollection")
public class PermissionCollectionDo extends DoEntity {

  protected static final String PERMISSION_COLLECTION_OBJECT_TYPE = "PermissionCollection";

  /**
   * String used to identify the Scout JS object created by this object model.
   */
  public DoValue<String> objectType() {
    return doValue("objectType");
  }

  public DoValue<PermissionCollectionType> type() {
    return doValue("type");
  }

  public DoValue<Map<String, ? extends Collection<PermissionDo>>> permissions() {
    return doValue("permissions");
  }

  /* **************************************************************************
   * CUSTOM CONVENIENCE TO DO FUNCTION
   * *************************************************************************/

  public static class ToPermissionCollectionDoFunction extends AbstractToPermissionCollectionDoFunction<IPermissionCollection, PermissionCollectionDo> {
    @Override
    public void apply(IPermissionCollection permissionCollection, PermissionCollectionDo permissionCollectionDo) {
      ToDoFunctionHelper toDoFunctionHelper = BEANS.get(ToDoFunctionHelper.class);
      permissionCollectionDo
          .withObjectType(PERMISSION_COLLECTION_OBJECT_TYPE)
          .withPermissions(permissionCollection.stream()
              .map(permission -> toDoFunctionHelper.toDo(permission, IToPermissionDoFunction.class))
              .filter(Objects::nonNull)
              .collect(Collectors.groupingBy(PermissionDo::getName, Collectors.toSet())))
          .withType(PermissionCollectionType.DEFAULT);
    }
  }

  @Order(4000)
  public static class ToAllPermissionCollectionDoFunction extends AbstractToPermissionCollectionDoFunction<AllPermissionCollection, PermissionCollectionDo> {
    @Override
    public void apply(AllPermissionCollection permissionCollection, PermissionCollectionDo permissionCollectionDo) {
      BEANS.get(ToPermissionCollectionDoFunction.class).apply(permissionCollection, permissionCollectionDo);
      permissionCollectionDo.withType(PermissionCollectionType.ALL);
    }
  }

  @Order(4000)
  public static class ToNonePermissionCollectionDoFunction extends AbstractToPermissionCollectionDoFunction<NonePermissionCollection, PermissionCollectionDo> {
    @Override
    public void apply(NonePermissionCollection permissionCollection, PermissionCollectionDo permissionCollectionDo) {
      BEANS.get(ToPermissionCollectionDoFunction.class).apply(permissionCollection, permissionCollectionDo);
      permissionCollectionDo.withType(PermissionCollectionType.NONE);
    }
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
  public PermissionCollectionDo withPermissions(Map<String, ? extends Collection<PermissionDo>> permissions) {
    permissions().set(permissions);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, ? extends Collection<PermissionDo>> getPermissions() {
    return permissions().get();
  }
}
