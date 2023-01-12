/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column} from '../../index';

export class CompactColumn extends Column<any> {
  constructor() {
    super();
    this.cssClass = 'compact-cell';
    this.htmlEnabled = true;
    this.width = 120;
    this.textWrap = true;
  }

  protected _onMoreLinkAction($row: JQuery, $moreLink: JQuery) {
    let $cell = this.table.$cell(this, $row);
    let $moreContent = $cell.find('.compact-cell-more-content');
    let contentVisible = !$moreContent.isVisible();
    $moreContent.setVisible(contentVisible);
    $moreLink.setVisible(!contentVisible);
    this.table.invalidateLayoutTree();
  }

  override onMouseUp(event: JQuery.MouseUpEvent, $row: JQuery) {
    // The more-link is not a regular app-link to not trigger the app link action when clicking on the more-link
    // (could interfere with other regular app-links)
    let $start = $(event.target);
    let $stop = $(event.delegateTarget);
    let $moreLink = $start.findUp($elem => $elem.hasClass('more-link'), $stop);
    if ($moreLink.length > 0) {
      this._onMoreLinkAction($row, $moreLink);
    }
  }
}
