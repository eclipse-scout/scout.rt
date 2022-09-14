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
import {FocusRule, keys, KeyStrokeContext, scout, ScoutKeyboardEvent, Tooltip, Widget, WidgetTooltipModel} from '../index';
import {FocusRuleType} from '../focus/FocusRule';

export default class WidgetTooltip extends Tooltip implements WidgetTooltipModel {
  declare model: WidgetTooltipModel;

  keyStrokeStopPropagationInterceptor: (event: ScoutKeyboardEvent) => void;
  withFocusContext: boolean;
  initialFocus: () => FocusRuleType;
  focusableContainer: boolean;
  content: Widget;
  $widgetContainer: JQuery;

  constructor() {
    super();

    this.$widgetContainer = null;
    this.content = null;
    this._addWidgetProperties(['content']);
    this.keyStrokeStopPropagationInterceptor = event => {
      if (scout.isOneOf(event.which, keys.ESC, keys.ENTER)) {
        return;
      }
      event.stopPropagation();
    };
    this.withFocusContext = true;
    this.initialFocus = () => FocusRule.AUTO;
    this.focusableContainer = false;
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    if (this.keyStrokeStopPropagationInterceptor) {
      this.keyStrokeContext.registerStopPropagationInterceptor(this.keyStrokeStopPropagationInterceptor);
    }
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

  setContent(content: Widget) {
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

    // Focus the widget
    // It is important that this happens after layouting and positioning, otherwise we'd focus an element
    // that is currently not on the screen. Which would cause the whole desktop to
    // be shifted for a few pixels.
    if (this.withFocusContext && this.content) {
      this.session.focusManager.installFocusContext(this.$widgetContainer, this.initialFocus());
    }
  }

  protected _removeContent() {
    if (this.content) {
      this.session.focusManager.uninstallFocusContext(this.$widgetContainer);
      this.content.remove();
    }
  }
}
