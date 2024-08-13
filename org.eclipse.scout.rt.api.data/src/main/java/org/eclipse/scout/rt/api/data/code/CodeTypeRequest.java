/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.code;

import java.util.Collection;
import java.util.Set;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.CodeTypeRequest")
public class CodeTypeRequest extends DoEntity {
  public DoSet<String> codeTypeIds() {
    return doSet("codeTypeIds");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeRequest withCodeTypeIds(Collection<? extends String> codeTypeIds) {
    codeTypeIds().updateAll(codeTypeIds);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeRequest withCodeTypeIds(String... codeTypeIds) {
    codeTypeIds().updateAll(codeTypeIds);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<String> getCodeTypeIds() {
    return codeTypeIds().get();
  }
}
