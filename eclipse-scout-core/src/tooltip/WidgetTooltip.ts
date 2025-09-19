/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, ObjectOrChildModel, Tooltip, Widget, WidgetTooltipEventMap, WidgetTooltipModel} from '../index';

export class WidgetTooltip extends Tooltip implements WidgetTooltipModel {
  declare model: WidgetTooltipModel;
  declare eventMap: WidgetTooltipEventMap;
  declare self: WidgetTooltip;

  content: Widget;
  $widgetContainer: JQuery;

  constructor() {
    super();

    this.$widgetContainer = null;
    this.content = null;
    this._addWidgetProperties(['content']);
    this.withFocusContext = true;
    this.focusableContainer = false;
    this._closeKeysWhenFocused = [keys.ESC, keys.ENTER];
  }

  protected override _render() {
    super._render();
    this.$container.addClass('widget-tooltip');
    this.$widgetContainer = this.$container.appendDiv('tooltip-widget-container');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderContent();
  }

  protected override _remove() {
    this._removeContent();
    super._remove();
  }

  setContent(content: ObjectOrChildModel<Widget>) {
    this.setProperty('content', content);
  }

  protected _renderContent() {
    if (this.content) {
      this.content.render(this.$widgetContainer);
      this.content.$container.addClass('widget');
      this.content.pack();
    }
    this.$widgetContainer.setVisible(!!this.content);
    if (!this.rendering) {
      this.position();
    }
    this.session.focusManager.validateFocus();
  }

  protected _removeContent() {
    if (this.content) {
      this.content.remove();
    }
  }
}
