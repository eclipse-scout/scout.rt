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

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.RemainingPagePathToActivate")
public class RemainingPagePathToActivateDo extends DoEntity {

  /**
   * Bookmark descriptor of the parent page. Used to identify the correct child page.
   */
  public DoValue<IBookmarkPageDo> parentBookmarkPage() {
    return doValue("parentBookmarkPage");
  }

  /**
   * The page path to activate. This path is relative to the parent page, which is sent along as a context element.
   */
  public DoList<IBookmarkPageDo> pagePath() {
    return doList("pagePath");
  }

  /**
   * Options to apply when activating the remaining bookmark
   */
  public DoValue<ActivateBookmarkOptionsDo> options() {
    return doValue("options");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  /**
   * See {@link #parentBookmarkPage()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public RemainingPagePathToActivateDo withParentBookmarkPage(IBookmarkPageDo parentBookmarkPage) {
    parentBookmarkPage().set(parentBookmarkPage);
    return this;
  }

  /**
   * See {@link #parentBookmarkPage()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public IBookmarkPageDo getParentBookmarkPage() {
    return parentBookmarkPage().get();
  }

  /**
   * See {@link #pagePath()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public RemainingPagePathToActivateDo withPagePath(Collection<? extends IBookmarkPageDo> pagePath) {
    pagePath().updateAll(pagePath);
    return this;
  }

  /**
   * See {@link #pagePath()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public RemainingPagePathToActivateDo withPagePath(IBookmarkPageDo... pagePath) {
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

  /**
   * See {@link #options()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public RemainingPagePathToActivateDo withOptions(ActivateBookmarkOptionsDo options) {
    options().set(options);
    return this;
  }

  /**
   * See {@link #options()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public ActivateBookmarkOptionsDo getOptions() {
    return options().get();
  }
}
