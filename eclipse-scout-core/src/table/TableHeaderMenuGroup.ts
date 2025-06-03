/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, AriaLabelledByInsertPosition, InitModelOf, scout, TableHeaderMenuGroupEventMap, TableHeaderMenuGroupModel, Widget, widgets} from '../index';

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
    this.children.forEach(child => {
      child.render();
      if (isGroupItem(child)) {
        this._installGroupItem(child);
      }
    });
    widgets.updateFirstLastMarker(this.children);
  }

  appendText(text: string) {
    this.text = this.session.text(this.textKey) + ' ' + text;
    if (this.rendered) {
      this._renderText();
    }
  }

  resetText() {
    let focusedItem = this._getFocusedGroupItem();
    if (focusedItem) {
      this.appendText(focusedItem.computeGroupSuffix());
    } else {
      this.setText(this.session.text(this.textKey));
    }
  }

  setText(text: string) {
    this.text = text;
    if (this.rendered) {
      this._renderText();
    }
  }

  protected _renderText() {
    this.$text.text(this.text);
  }

  protected _getFocusedGroupItem(): TableHeaderMenuGroupItem {
    if (!this.rendered) {
      return null;
    }
    let focusedWidget = scout.widget(this.$container.activeElement());
    if (this.has(focusedWidget) && isGroupItem(focusedWidget)) {
      return focusedWidget;
    }
    return null;
  }

  setLast(last: boolean) {
    this.setProperty('last', last);
  }

  protected _renderLast() {
    this.$container.toggleClass('last', this.last);
  }

  /**
   * Installs hover and mouse over handlers to update the text of the group.
   */
  protected _installGroupItem(item: TableHeaderMenuGroupItem) {
    // Remove aria-label because aria-labelledby points to a more sophisticated text
    aria.label(item.$container, null);
    // link item with the group header, the header is updated with the text of the action
    aria.linkElementWithLabel($(item.getFocusableElement()), this.$text, AriaLabelledByInsertPosition.FRONT, true);

    item.$container
      .on('focusin mouseenter', () => this.appendText(item.computeGroupSuffix()))
      .on('focusout mouseleave', () => {
        if (!item.isFocused()) {
          this.resetText();
        }
      });
  }
}

export interface TableHeaderMenuGroupItem extends Widget {
  /**
   * @returns the suffix to append to the text of a {@link TableHeaderMenuGroup}.
   */
  computeGroupSuffix(): string;
}

function isGroupItem(item: Widget): item is TableHeaderMenuGroupItem {
  return Reflect.has(item, 'computeGroupSuffix');
}
