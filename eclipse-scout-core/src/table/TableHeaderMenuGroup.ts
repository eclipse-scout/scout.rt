/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, scout, TableHeaderMenuGroupEventMap, TableHeaderMenuGroupModel, Widget, widgets} from '../index';

export class TableHeaderMenuGroup extends Widget implements TableHeaderMenuGroupModel {
  declare model: TableHeaderMenuGroupModel;
  declare eventMap: TableHeaderMenuGroupEventMap;
  declare self: TableHeaderMenuGroup;

  text: string;
  textKey: string;
  last: boolean;
  $text: JQuery;

  constructor() {
    super();
    this.text = null;
    this.textKey = null;
    this.visible = true;
    this.last = false;
  }

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    this.text = scout.nvl(this.text, this.session.text(this.textKey));
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('table-header-menu-group buttons');
    this.$text = this.$container.appendDiv('table-header-menu-group-text');
    if (this.cssClass) {
      this.$container.addClass(this.cssClass);
    }
    this._renderText();
    this.children.forEach(child => child.render());
    widgets.updateFirstLastMarker(this.children);
  }

  appendText(text: string) {
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

  protected _renderText() {
    this.$text.text(this.text);
  }

  setLast(last: boolean) {
    this.setProperty('last', last);
  }

  protected _renderLast() {
    this.$container.toggleClass('last', this.last);
  }
}
