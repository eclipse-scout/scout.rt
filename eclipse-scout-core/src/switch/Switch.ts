/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, strings, SwitchEventMap, SwitchModel, tooltips, Widget} from '../index';

export class Switch extends Widget implements SwitchModel {
  declare model: SwitchModel;
  declare eventMap: SwitchEventMap;
  declare self: Switch;

  activated: boolean;
  label: string;
  htmlEnabled: boolean;
  tooltipText: string;

  $label: JQuery;
  $button: JQuery;

  constructor() {
    super();

    this.activated = false;
    this.label = null;
    this.htmlEnabled = false;
    this.tooltipText = null;

    this.$label = null;
    this.$button = null;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.setTooltipText(model.tooltipText);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('switch');
    this.$label = this.$container.appendDiv('switch-label');
    this.$button = this.$container.appendDiv('switch-button')
      .on('click', this._onSwitchButtonClick.bind(this));
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderActivated();
    this._renderLabel();
    this._renderTooltipText();
  }

  protected _onSwitchButtonClick(e: JQuery.ClickEvent<HTMLDivElement>) {
    if (!this.enabledComputed) {
      return;
    }
    let event = this.trigger('switch');
    if (!event.defaultPrevented) {
      this.setActivated(!this.activated);
    }
    // When the action is clicked the user wants to execute the action and not see the tooltip -> cancel the task
    // If it is already displayed it will stay
    tooltips.cancel(this.$container);
  }

  setLabel(label: string) {
    this.setProperty('label', label);
  }

  protected _renderLabel() {
    if (this.htmlEnabled) {
      this.$label.html(this.label);
    } else {
      this.$label.text(this.label);
    }
  }

  setActivated(activated: boolean) {
    this.setProperty('activated', activated);
  }

  protected _renderActivated() {
    this.$button.toggleClass('activated', this.activated);
  }

  setTooltipText(tooltipText: string) {
    this.setProperty('tooltipText', tooltipText);
  }

  protected _renderTooltipText() {
    if (strings.hasText(this.tooltipText)) {
      tooltips.install(this.$container, {
        parent: this,
        text: this.tooltipText
      });
    } else {
      tooltips.uninstall(this.$container);
    }
  }
}
