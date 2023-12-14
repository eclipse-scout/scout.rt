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

@TypeName("scout.ClassSignature")
public class ClassSignatureDo extends DoEntity {

  public DoValue<String> name() {
    return doValue("name");
  }

  public DoValue<Integer> modifiers() {
    return doValue("modifier");
  }

  public DoValue<String> superclass() {
    return doValue("superclass");
  }

  public DoList<String> interfaces() {
    return doList("interfaces");
  }

  public DoList<MethodSignatureDo> methods() {
    return doList("methods");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ClassSignatureDo withName(String name) {
    name().set(name);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getName() {
    return name().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ClassSignatureDo withModifiers(Integer modifiers) {
    modifiers().set(modifiers);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getModifiers() {
    return modifiers().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ClassSignatureDo withSuperclass(String superclass) {
    superclass().set(superclass);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getSuperclass() {
    return superclass().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ClassSignatureDo withInterfaces(Collection<? extends String> interfaces) {
    interfaces().updateAll(interfaces);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ClassSignatureDo withInterfaces(String... interfaces) {
    interfaces().updateAll(interfaces);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<String> getInterfaces() {
    return interfaces().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ClassSignatureDo withMethods(Collection<? extends MethodSignatureDo> methods) {
    methods().updateAll(methods);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ClassSignatureDo withMethods(MethodSignatureDo... methods) {
    methods().updateAll(methods);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<MethodSignatureDo> getMethods() {
    return methods().get();
  }
}
