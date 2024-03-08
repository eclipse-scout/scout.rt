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
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.CodeTypeUpdateMessage")
public class CodeTypeUpdateMessageDo extends DoEntity {

  public DoList<CodeTypeDo> codeTypes() {
    return doList("codeTypes");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeUpdateMessageDo withCodeTypes(Collection<? extends CodeTypeDo> codeTypes) {
    codeTypes().updateAll(codeTypes);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeUpdateMessageDo withCodeTypes(CodeTypeDo... codeTypes) {
    codeTypes().updateAll(codeTypes);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<CodeTypeDo> getCodeTypes() {
    return codeTypes().get();
  }
}
