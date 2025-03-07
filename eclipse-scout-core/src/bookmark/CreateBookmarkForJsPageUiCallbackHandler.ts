/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BookmarkDo, Desktop, Page, scout, UiCallbackHandler, UiCallbackParam} from '../index';

export class CreateBookmarkForJsPageUiCallbackHandler implements UiCallbackHandler {

  handle(param: UiCallbackParam): JQuery.Promise<BookmarkDo> {
    const desktop = scout.assertInstance(param.owner, Desktop);
    const bookmarkSupport = desktop.bookmarkSupport;
    const contextElements = scout.assertValue(param.contextElements, 'Missing context elements');
    const page = contextElements.getSingle('page').getElement(Page);

    return bookmarkSupport.createBookmark(page);
  }
}
