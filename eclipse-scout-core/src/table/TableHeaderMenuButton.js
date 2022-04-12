/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, ActionExecKeyStroke} from '../index';

export default class TableHeaderMenuButton extends Action {

  constructor() {
    super();
    this.textVisible = false;
    this.tabbable = true;
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke([new ActionExecKeyStroke(this)]);
  }

  _render() {
    super._render();
    this.$container = this.$container.addClass('table-header-menu-command button')
      .unfocusable()
      .on('mouseenter', this._onMouseOver.bind(this))
      .on('mouseleave', this._onMouseOut.bind(this));
    this.$icon = this.$container.appendSpan('icon font-icon');
  }

  _renderProperties() {
    super._renderProperties();
    this._renderToggleAction();
  }

  // Show 'remove' text when button is already selected
  _onMouseOver() {
    let text = this.selected ?
      this.session.text('ui.remove') : this.text;
    this.parent.appendText(text);
  }

  _onMouseOut() {
    this.parent.resetText();
  }

  _renderToggleAction() {
    this.$container.toggleClass('togglable', this.toggleAction);
  }

  /**
   * @override
   */
  _renderIconId() {
    if (this.iconId) {
      this.$icon.attr('data-icon', this.iconId);
    } else {
      this.$icon.removeAttr('data-icon');
    }
  }
}
