/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {icons, NavigateButton} from '../../../index';
import $ from 'jquery';

export default class NavigateUpButton extends NavigateButton {

  constructor() {
    super();
    this._defaultIconId = icons.ANGLE_UP;
    this._defaultText = 'ui.Up';
    this.iconId = this._defaultIconId;
    this.keyStroke = 'backspace';
  }

  _render() {
    super._render();
    this.$container.addClass('up');
  }

  _isDetail() {
    // Button is in "detail mode" if there are both detail form and detail table visible and detail form _is_ hidden.
    return !!(this.node.detailFormVisible && this.node.detailForm &&
      this.node.detailTableVisible && this.node.detailTable && !this.node.detailFormVisibleByUi);
  }

  _toggleDetail() {
    return true;
  }

  /**
   * Returns true when current node has either a parentNode or if current node is a
   * top-level node without a parent and the outline has a default detail-form.
   */
  _buttonEnabled() {
    let parentNode = this.node.parentNode;
    return !!parentNode || !!this.outline.defaultDetailForm || !!this.outline.outlineOverview;
  }

  _drill() {
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
