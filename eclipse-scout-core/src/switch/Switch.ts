/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {strings, SwitchEventMap, SwitchModel, tooltips, Widget} from '../index';

export default class Switch extends Widget implements SwitchModel {
  declare model: SwitchModel;
  declare eventMap: SwitchEventMap;

  activated: boolean;
  label: string;
  htmlEnabled: boolean;
  tooltipText: string;

  $label: JQuery<HTMLDivElement>;
  $button: JQuery<HTMLDivElement>;

  constructor() {
    super();

    this.activated = false;
    this.label = null;
    this.htmlEnabled = false;
    this.tooltipText = null;

    this.$label = null;
    this.$button = null;
  }

  protected override _init(model: SwitchModel) {
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
