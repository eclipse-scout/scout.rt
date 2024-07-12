/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Device, EnumObject, HtmlComponent, InitModelOf, KeyStrokeContext, objects, scout, strings, SwitchEventMap, SwitchModel, SwitchNavigationKeyStroke, SwitchToggleKeyStroke, tooltips, Widget} from '../index';

export class Switch extends Widget implements SwitchModel {
  declare model: SwitchModel;
  declare eventMap: SwitchEventMap;
  declare self: Switch;

  static DisplayStyle = {
    DEFAULT: 'default',
    SLIDER: 'slider'
  } as const;

  activated: boolean;
  label: string;
  labelHtmlEnabled: boolean;
  labelVisible: boolean;
  tooltipText: string;
  iconVisible: boolean;
  displayStyle: SwitchDisplayStyle;
  tabbable: boolean;

  $label: JQuery;
  $button: JQuery;

  constructor() {
    super();

    this.activated = false;
    this.label = null;
    this.labelHtmlEnabled = false;
    this.labelVisible = null;
    this.tooltipText = null;
    this.iconVisible = false;
    this.displayStyle = Switch.DisplayStyle.DEFAULT;
    this.tabbable = false;

    this.$label = null;
    this.$button = null;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.resolveTextKeys(['label', 'tooltipText']);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('switch')
      .on('mousedown', this._onSwitchMouseDown.bind(this));
    this.htmlComp = HtmlComponent.install(this.$container, this.session);

    this.$label = this.$container.appendDiv('switch-label');
    this.$button = this.$container.appendDiv('switch-button');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderActivated();
    this._renderLabel();
    this._renderTooltipText();
    this._renderIconVisible();
    this._renderDisplayStyle();
    this._renderTabbable();
  }

  protected override _remove() {
    this.$label = null;
    this.$button = null;
    super._remove();
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    if (this.rendered) {
      this._renderTabbable();
    }
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.registerKeyStrokes([
      new SwitchToggleKeyStroke(this),
      new SwitchNavigationKeyStroke(this)
    ]);
  }

  setActivated(activated: boolean) {
    this.setProperty('activated', activated);
  }

  protected _renderActivated() {
    this.$button.toggleClass('activated', !!this.activated);
  }

  setLabel(label: string) {
    this.setProperty('label', label);
  }

  protected _renderLabel() {
    if (this.labelHtmlEnabled) {
      this.$label.html(this.label);
    } else {
      this.$label.text(this.label);
    }
    this._renderLabelVisible();
  }

  setLabelHtmlEnabled(labelHtmlEnabled: boolean) {
    this.setProperty('labelHtmlEnabled', labelHtmlEnabled);
  }

  protected _renderLabelHtmlEnabled() {
    this._renderLabel();
  }

  setLabelVisible(labelVisible: boolean) {
    this.setProperty('labelVisible', labelVisible);
  }

  protected _renderLabelVisible() {
    let labelVisible = objects.isNullOrUndefined(this.labelVisible) ? strings.hasText(this.label) : !!this.labelVisible;
    this.$label.setVisible(labelVisible);
    this.invalidateLayoutTree();
  }

  setTooltipText(tooltipText: string) {
    this.setProperty('tooltipText', tooltipText);
  }

  protected _renderTooltipText() {
    if (strings.hasText(this.tooltipText)) {
      tooltips.install(this.$container, {
        parent: this,
        text: this.tooltipText,
        $anchor: this.$button
      });
    } else {
      tooltips.uninstall(this.$container);
    }
  }

  setIconVisible(iconVisible: boolean) {
    this.setProperty('iconVisible', iconVisible);
  }

  protected _renderIconVisible() {
    this.$button.toggleClass('icon-visible', !!this.iconVisible);
  }

  setDisplayStyle(displayStyle: SwitchDisplayStyle) {
    this.setProperty('displayStyle', displayStyle);
  }

  protected _renderDisplayStyle() {
    this.$container.toggleClass('style-default', this.displayStyle === Switch.DisplayStyle.DEFAULT);
    this.$container.toggleClass('style-slider', this.displayStyle === Switch.DisplayStyle.SLIDER);
    this.invalidateLayoutTree();
  }

  setTabbable(tabbable: boolean) {
    this.setProperty('tabbable', tabbable);
  }

  protected _renderTabbable() {
    let tabbable = this.tabbable && this.enabledComputed && !Device.get().supportsOnlyTouch();
    this.$container.setTabbable(tabbable);
    this.$container.toggleClass('unfocusable', !!tabbable);
  }

  protected _onSwitchMouseDown(event: JQuery.MouseDownEvent) {
    if (event.button !== 0) { // 0 = main button
      return;
    }
    this.toggleSwitch(event);
  }

  /**
   * @param originalEvent original event that caused the toggle
   * @param newValue if set, the `activated` property is set to this value.
   *           Otherwise, it is set to opposite of the current value.
   */
  toggleSwitch(originalEvent?: JQuery.Event, activated?: boolean) {
    if (!this.enabledComputed) {
      return;
    }
    let oldValue = this.activated;
    let newValue = scout.nvl(activated, !this.activated);
    let event = this.trigger('switch', {
      originalEvent: originalEvent,
      oldValue: oldValue,
      newValue: newValue
    });
    if (!event.defaultPrevented) {
      this.setActivated(newValue);
    }
    // When the action is clicked the user wants to execute the action and not see the tooltip -> cancel the task
    // If it is already displayed it will stay
    this.$container && tooltips.cancel(this.$container);
  }
}

export type SwitchDisplayStyle = EnumObject<typeof Switch.DisplayStyle>;
