/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BookmarkSupport, Desktop, Outline, Page, RemainingPagePathToActivateDo, scout, UiCallbackHandler, UiCallbackParam} from '../index';

export class ActivateRemainingBookmarkUiCallbackHandler implements UiCallbackHandler {

  handle(param: UiCallbackParam): JQuery.Promise<void> {
    const desktop = scout.assertInstance(param.owner, Desktop, 'owner is not of type Desktop');
    const data = scout.assertInstance(param.data, RemainingPagePathToActivateDo, 'data is not of type RemainingPagePathToActivateDo');
    const contextElements = scout.assertValue(param.contextElements, 'Missing context elements');

    let parentPageContextElement = contextElements.optSingle('parentPage');
    let parentOutline = parentPageContextElement?.getWidget(Outline);
    let parentPage = parentPageContextElement?.optElement(Page);

    return BookmarkSupport.get(desktop.session).activateBookmarkLocal({
      parentOutline: parentOutline,
      parentPage: parentPage,
      parentBookmarkPage: data.parentBookmarkPage,
      pagePath: data.pagePath,
      applyParentBookmarkPage: true
    });
  }
}
