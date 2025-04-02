/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.page;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.ImportSearchData")
public class ImportSearchDataDo extends DoEntity {

  public DoValue<ISearchDo> searchData() {
    return doValue("searchData");
  }

  public DoValue<Boolean> markAsSaved() {
    return doValue("markAsSaved");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ImportSearchDataDo withSearchData(ISearchDo searchData) {
    searchData().set(searchData);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ISearchDo getSearchData() {
    return searchData().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ImportSearchDataDo withMarkAsSaved(Boolean markAsSaved) {
    markAsSaved().set(markAsSaved);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getMarkAsSaved() {
    return markAsSaved().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isMarkAsSaved() {
    return nvl(getMarkAsSaved());
  }
}
