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
import {scout, Widget, widgets} from '../index';

export default class TableHeaderMenuGroup extends Widget {

  constructor() {
    super();
    this.text = null;
    this.textKey = null;
    this.visible = true;
    this.last = false;
  }

  _init(options) {
    super._init(options);
    this.text = scout.nvl(this.text, this.session.text(this.textKey));
  }

  _render() {
    this.$container = this.$parent.appendDiv('table-header-menu-group buttons');
    this.$text = this.$container.appendDiv('table-header-menu-group-text');
    if (this.cssClass) {
      this.$container.addClass(this.cssClass);
    }
    this._renderText();
    this.children.forEach(child => {
      child.render();
    }, this);
    widgets.updateFirstLastMarker(this.children);
  }

  appendText(text) {
    this.text = this.session.text(this.textKey) + ' ' + text;
    if (this.rendered) {
      this._renderText();
    }
  }

  resetText() {
    this.text = this.session.text(this.textKey);
    if (this.rendered) {
      this._renderText();
    }
  }

  _renderText() {
    this.$text.text(this.text);
  }

  setLast(last) {
    this.setProperty('last', last);
  }

  _renderLast() {
    this.$container.toggleClass('last', this.last);
  }
}
