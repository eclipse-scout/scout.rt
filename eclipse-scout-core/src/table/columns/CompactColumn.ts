/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Column} from '../../index';

export default class CompactColumn extends Column {
  constructor() {
    super();
    this.cssClass = 'compact-cell';
    this.htmlEnabled = true;
    this.width = 120;
    this.textWrap = true;
  }

  _onMoreLinkAction($row, $moreLink) {
    let $cell = this.table.$cell(this, $row);
    let $moreContent = $cell.find('.compact-cell-more-content');
    let contentVisible = !$moreContent.isVisible();
    $moreContent.setVisible(contentVisible);
    $moreLink.setVisible(!contentVisible);
    this.table.invalidateLayoutTree();
  }

  /**
   * @override
   */
  onMouseUp(event, $row) {
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
