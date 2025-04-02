/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.bookmark;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.api.data.page.IPageParamDo;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

@TypeName("scout.NodeBookmarkPage")
@TypeVersion(Scout_25_2_001.class)
public class NodeBookmarkPageDo extends DoEntity implements IBookmarkPageDo {

  public DoValue<IPageParamDo> pageParam() {
    return doValue(PAGE_PARAM);
  }

  public DoValue<String> displayText() {
    return doValue("displayText");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public NodeBookmarkPageDo withPageParam(IPageParamDo pageParam) {
    pageParam().set(pageParam);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public IPageParamDo getPageParam() {
    return pageParam().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public NodeBookmarkPageDo withDisplayText(String displayText) {
    displayText().set(displayText);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public String getDisplayText() {
    return displayText().get();
  }
}
