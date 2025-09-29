/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages.js;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.api.data.page.IPageParamDo;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.LoadChildPagesHybridAction")
public class LoadChildPagesHybridActionDo extends DoEntity {

  public DoList<IPageParamDo> pageParams() {
    return doList("pageParams");
  }

  public DoValue<Boolean> replace() {
    return doValue("replace");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public LoadChildPagesHybridActionDo withPageParams(Collection<? extends IPageParamDo> pageParams) {
    pageParams().updateAll(pageParams);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public LoadChildPagesHybridActionDo withPageParams(IPageParamDo... pageParams) {
    pageParams().updateAll(pageParams);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<IPageParamDo> getPageParams() {
    return pageParams().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public LoadChildPagesHybridActionDo withReplace(Boolean replace) {
    replace().set(replace);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getReplace() {
    return replace().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isReplace() {
    return nvl(getReplace());
  }
}
