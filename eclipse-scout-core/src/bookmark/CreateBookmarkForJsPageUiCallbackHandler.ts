/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BookmarkDoBuilderOptionsDo, BookmarkSupport, Desktop, IBookmarkDo, Page, scout, UiCallbackHandler, UiCallbackParam} from '../index';

export class CreateBookmarkForJsPageUiCallbackHandler implements UiCallbackHandler {

  handle(param: UiCallbackParam): JQuery.Promise<IBookmarkDo> {
    const desktop = scout.assertInstance(param.owner, Desktop, 'owner is not of type Desktop');
    const options = scout.assertInstance(param.data, BookmarkDoBuilderOptionsDo, 'data is not of type BookmarkDoBuilderOptionsDo');
    const contextElements = scout.assertValue(param.contextElements, 'Missing context elements');
    const page = contextElements.getSingle('page').getElement(Page);

    if (!page.childrenLoaded) {
      // If children are not loaded, we assume that the bookmark has not yet been opened by the user. This can happen if they
      // update the definition of a bookmark at folder level without opening the bookmarked page before. In this case, updating
      // the definition would replace the previous configured search with an empty search. See #229618 for details.
      return null;
    }
    return BookmarkSupport.get(desktop.session).createBookmark({page, ...options});
  }
}
