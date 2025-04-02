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

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

/**
 * Bookmark definition that can only be used to open the bookmark in page, it cannot be opened in the outline (contains
 * no information of outline or page path).
 * <p>
 * There are some cases where it is not possible to create an outline bookmark because pages along the path are not
 * bookmarkable. In such a scenario a page bookmark is created.
 */
@TypeName("scout.PageBookmarkDefinition")
@TypeVersion(Scout_25_2_001.class)
public class PageBookmarkDefinitionDo extends DoEntity implements IBookmarkDefinitionDo {

  public DoValue<IBookmarkPageDo> bookmarkedPage() {
    return doValue("bookmarkedPage");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public PageBookmarkDefinitionDo withBookmarkedPage(IBookmarkPageDo bookmarkedPage) {
    bookmarkedPage().set(bookmarkedPage);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public IBookmarkPageDo getBookmarkedPage() {
    return bookmarkedPage().get();
  }
}
