/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ActivateBookmarkRequestDo, BookmarkSupport, Desktop, Outline, Page, scout, UiCallbackHandler, UiCallbackParam} from '../index';

export class OpenBookmarkInOutlineUiCallbackHandler implements UiCallbackHandler {

  handle(param: UiCallbackParam): JQuery.Promise<any> {
    const desktop = scout.assertInstance(param.owner, Desktop);
    const data = scout.assertInstance(param.data, ActivateBookmarkRequestDo);
    const contextElements = scout.assertValue(param.contextElements, 'Missing context elements');

    let parentPageContextElement = contextElements.optSingle('parentPage');
    let parentOutline = parentPageContextElement?.getWidget(Outline);
    let parentPage = parentPageContextElement?.optElement(Page);

    return BookmarkSupport.get(desktop.session).openBookmarkLocal({
      parentOutline: parentOutline,
      parentPage: parentPage,
      parentBookmarkPage: data.parentBookmarkPage,
      pagePath: data.pagePath
    }).then(() => null);
  }
}
