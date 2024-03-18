/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {icons, NavigateButton} from '../../../index';
import $ from 'jquery';

export class NavigateUpButton extends NavigateButton {

  constructor() {
    super();
    this._defaultIconId = icons.ANGLE_UP;
    this._defaultText = 'ui.Up';
    this.iconId = this._defaultIconId;
    this.keyStroke = 'backspace';
  }

  protected override _render() {
    super._render();
    this.$container.addClass('up');
  }

  protected _isDetail(): boolean {
    // Button is in "detail mode" if there are both detail form and detail table visible and detail form _is_ hidden.
    return this._hasDetailForm() && this._hasDetailTable() && !this.node.detailFormVisibleByUi;
  }

  protected _toggleDetail(): boolean {
    return true;
  }

  /**
   * Returns true when current node has either a parentNode or if current node is a
   * top-level node without a parent and the outline has a default detail-form.
   */
  protected _buttonEnabled(): boolean {
    let parentNode = this.node.parentNode;
    return !!parentNode || !!this.outline.defaultDetailForm || !!this.outline.outlineOverview;
  }

  protected _drill() {
    let parentNode = this.node.parentNode;
    if (parentNode) {
      $.log.isDebugEnabled() && $.log.debug('drill up to node ' + parentNode);
      this.outline.navigateUpInProgress = true;
      this.outline.selectNodes(parentNode);
      this.outline.collapseNode(parentNode, {
        collapseChildNodes: true
      });
    } else {
      $.log.isDebugEnabled() && $.log.debug('drill up to top');
      this.outline.navigateToTop();
    }
  }
}
