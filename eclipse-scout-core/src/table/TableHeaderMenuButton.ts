/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, ActionExecKeyStroke, NumberColumnAggregationFunction, NumberColumnBackgroundEffect, TableHeaderMenuButtonModel, TableHeaderMenuGroup, TableHeaderMenuGroupItem} from '../index';

export class TableHeaderMenuButton extends Action implements TableHeaderMenuButtonModel, TableHeaderMenuGroupItem {
  declare parent: TableHeaderMenuGroup;
  declare model: TableHeaderMenuButtonModel;

  aggregation: NumberColumnAggregationFunction;
  backgroundEffect: NumberColumnBackgroundEffect;
  direction: string;
  additional: boolean;

  $icon: JQuery<HTMLSpanElement>;

  constructor() {
    super();
    this.textVisible = false;
    this.tabbable = true;
    this.actionStyle = Action.ActionStyle.BUTTON;
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke(new ActionExecKeyStroke(this));
  }

  protected override _render() {
    super._render();
    this.$container = this.$container.addClass('table-header-menu-command button').unfocusable();
    this.$icon = this.$container.appendSpan('icon font-icon');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderToggleAction();
  }

  override _renderToggleAction() {
    super._renderToggleAction();
    this.$container.toggleClass('togglable', this.toggleAction);
  }

  computeGroupSuffix() {
    // Show 'remove' text when button is selected
    return this.selected ? this.session.text('ui.remove') : this.text;
  }

  protected override _renderIconId() {
    if (this.iconId) {
      this.$icon.attr('data-icon', this.iconId);
    } else {
      this.$icon.removeAttr('data-icon');
    }
  }
}
