/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BookmarkSupport, Desktop, IBookmarkDo, Page, scout, UiCallbackHandler, UiCallbackParam} from '../index';

export class ApplyBookmarkToJsPageUiCallbackHandler implements UiCallbackHandler {

  handle(param: UiCallbackParam): JQuery.Promise<void> {
    const desktop = scout.assertInstance(param.owner, Desktop, 'owner is not of type Desktop');
    const bookmark = scout.assertValue(param.data, 'Missing bookmark') as IBookmarkDo;
    const contextElements = scout.assertValue(param.contextElements, 'Missing context elements');
    const page = contextElements.getSingle('page').getElement(Page);

    return BookmarkSupport.get(desktop.session).applyBookmarkToPageAndReload(page, bookmark);
  }
}
