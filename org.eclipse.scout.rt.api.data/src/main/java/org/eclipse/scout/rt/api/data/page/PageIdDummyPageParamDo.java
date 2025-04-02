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
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

/**
 * Page param that is used by bookmarks to handle pages that do not provide a {@link IPageParamDo}. It stores the pages
 * ID so it can be identified when opening a bookmark in outline.
 */
@TypeName("scout.PageIdDummyPageParam")
@TypeVersion(Scout_25_2_001.class)
public class PageIdDummyPageParamDo extends DoEntity implements IPageParamDo {

  public DoValue<PageId> pageId() {
    return doValue("pageId");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public PageIdDummyPageParamDo withPageId(PageId pageId) {
    pageId().set(pageId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PageId getPageId() {
    return pageId().get();
  }
}
