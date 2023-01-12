/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, ActionExecKeyStroke, NumberColumnAggregationFunction, NumberColumnBackgroundEffect, TableHeaderMenuButtonModel, TableHeaderMenuGroup} from '../index';

export class TableHeaderMenuButton extends Action implements TableHeaderMenuButtonModel {
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
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke(new ActionExecKeyStroke(this));
  }

  protected override _render() {
    super._render();
    this.$container = this.$container.addClass('table-header-menu-command button')
      .unfocusable()
      .on('mouseenter', this._onMouseOver.bind(this))
      .on('mouseleave', this._onMouseOut.bind(this));
    this.$icon = this.$container.appendSpan('icon font-icon');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderToggleAction();
  }

  // Show 'remove' text when button is already selected
  protected _onMouseOver() {
    let text = this.selected ? this.session.text('ui.remove') : this.text;
    this.parent.appendText(text);
  }

  protected _onMouseOut() {
    this.parent.resetText();
  }

  protected _renderToggleAction() {
    this.$container.toggleClass('togglable', this.toggleAction);
  }

  protected override _renderIconId() {
    if (this.iconId) {
      this.$icon.attr('data-icon', this.iconId);
    } else {
      this.$icon.removeAttr('data-icon');
    }
  }
}
