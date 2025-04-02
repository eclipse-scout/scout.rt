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

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.api.data.page.OutlineId;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

/**
 * Bookmark definition that can be activated in outline, displaying the full path to the bookmarked page.
 */
@TypeName("scout.OutlineBookmarkDefinition")
@TypeVersion(Scout_25_2_001.class)
public class OutlineBookmarkDefinitionDo extends DoEntity implements IBookmarkDefinitionDo {

  public DoValue<OutlineId> outlineId() {
    return doValue("outlineId");
  }

  /**
   * Ordered path of (node/table) pages leading to the bookmarked page, not including said page.
   */
  public DoList<IBookmarkPageDo> pagePath() {
    return doList("pagePath");
  }

  public DoValue<IBookmarkPageDo> bookmarkedPage() {
    return doValue("bookmarkedPage");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public OutlineBookmarkDefinitionDo withOutlineId(OutlineId outlineId) {
    outlineId().set(outlineId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public OutlineId getOutlineId() {
    return outlineId().get();
  }

  /**
   * See {@link #pagePath()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public OutlineBookmarkDefinitionDo withPagePath(Collection<? extends IBookmarkPageDo> pagePath) {
    pagePath().updateAll(pagePath);
    return this;
  }

  /**
   * See {@link #pagePath()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public OutlineBookmarkDefinitionDo withPagePath(IBookmarkPageDo... pagePath) {
    pagePath().updateAll(pagePath);
    return this;
  }

  /**
   * See {@link #pagePath()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public List<IBookmarkPageDo> getPagePath() {
    return pagePath().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public OutlineBookmarkDefinitionDo withBookmarkedPage(IBookmarkPageDo bookmarkedPage) {
    bookmarkedPage().set(bookmarkedPage);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public IBookmarkPageDo getBookmarkedPage() {
    return bookmarkedPage().get();
  }
}
