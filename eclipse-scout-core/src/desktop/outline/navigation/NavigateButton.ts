/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, Menu} from '../../../index';
import $ from 'jquery';

/**
 * The outline navigation works mostly browser-side. The navigation logic is implemented in JavaScript.
 * When a navigation button is clicked, we process that click browser-side first and send an event to
 * the server which nodes have been selected. We do that for better user experience. In a first attempt
 * the whole navigation logic was on the server, which caused a lag and flickering in the UI.
 *
 * @abstract
 */
export default class NavigateButton extends Menu {

  constructor() {
    super();

    this.node = null;
    this.outline = null;
    this.actionStyle = Action.ActionStyle.BUTTON;
    this._addCloneProperties(['node', 'outline', 'altKeyStrokeContext']);
    this.inheritAccessibility = false;
  }

  /**
   * @override
   */
  _render() {
    if (this.overflow) {
      this.text = this.session.text(this._defaultText);
      this.iconId = null;
    } else {
      this.text = null;
      this.iconId = this._defaultIconId;
    }
    this.updateEnabled();
    super._render();
    this.$container.addClass('navigate-button small');
    this.altKeyStrokeContext.registerKeyStroke(this);
  }

  /**
   * @override Action.js
   */
  _remove() {
    super._remove();
    this.altKeyStrokeContext.unregisterKeyStroke(this);
  }

  _setDetailVisible() {
    let detailVisible = this._toggleDetail();
    $.log.isDebugEnabled() && $.log.debug('show detail-' + (detailVisible ? 'form' : 'table'));
    this.outline.setDetailFormVisibleByUi(this.node, detailVisible);
  }

  /**
   * @override Menu.js
   */
  _doAction() {
    super._doAction();
    if (this._isDetail()) {
      this._setDetailVisible();
    } else {
      this._drill();
    }
  }

  /**
   * Called when enabled state must be re-calculated and probably rendered.
   */
  updateEnabled() {
    this.setEnabled(this._buttonEnabled());
  }
}
