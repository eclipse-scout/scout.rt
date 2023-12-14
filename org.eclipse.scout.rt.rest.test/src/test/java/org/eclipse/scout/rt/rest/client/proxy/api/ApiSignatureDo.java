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
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.ApiSignature")
public class ApiSignatureDo extends DoEntity {

  public DoList<ClassSignatureDo> classes() {
    return doList("classes");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ApiSignatureDo withClasses(Collection<? extends ClassSignatureDo> classes) {
    classes().updateAll(classes);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ApiSignatureDo withClasses(ClassSignatureDo... classes) {
    classes().updateAll(classes);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<ClassSignatureDo> getClasses() {
    return classes().get();
  }
}
