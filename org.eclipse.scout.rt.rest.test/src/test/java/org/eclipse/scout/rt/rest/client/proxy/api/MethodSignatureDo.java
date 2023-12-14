/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.client.proxy.api;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.MethodSignature")
public class MethodSignatureDo extends DoEntity {

  public DoValue<String> name() {
    return doValue("name");
  }

  public DoValue<Integer> modifiers() {
    return doValue("modifiers");
  }

  public DoValue<String> returnType() {
    return doValue("returnType");
  }

  public DoList<String> parameterTypes() {
    return doList("parameterTypes");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public MethodSignatureDo withName(String name) {
    name().set(name);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getName() {
    return name().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public MethodSignatureDo withModifiers(Integer modifiers) {
    modifiers().set(modifiers);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getModifiers() {
    return modifiers().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public MethodSignatureDo withReturnType(String returnType) {
    returnType().set(returnType);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getReturnType() {
    return returnType().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public MethodSignatureDo withParameterTypes(Collection<? extends String> parameterTypes) {
    parameterTypes().updateAll(parameterTypes);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public MethodSignatureDo withParameterTypes(String... parameterTypes) {
    parameterTypes().updateAll(parameterTypes);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<String> getParameterTypes() {
    return parameterTypes().get();
  }
}
