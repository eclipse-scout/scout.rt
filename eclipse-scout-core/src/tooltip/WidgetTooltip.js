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
import {FocusRule, keys, KeyStrokeContext, scout, Tooltip} from '../index';

export default class WidgetTooltip extends Tooltip {

  constructor() {
    super();

    this.$widgetContainer = null;
    this.widget = null;
    this._addWidgetProperties(['widget']);

    // Default interceptor that stops the propagation for all key strokes except ESCAPE and ENTER.
    // Otherwise, the tooltip would be destroyed for all key strokes that bubble up to the
    // root (see global document listener in Tooltip.js).
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

  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    if (this.keyStrokeStopPropagationInterceptor) {
      this.keyStrokeContext.registerStopPropagationInterceptor(this.keyStrokeStopPropagationInterceptor);
    }
  }

  _render() {
    super._render();
    this.$container.addClass('widget-tooltip');
    this.$widgetContainer = this.$container.appendDiv('tooltip-widget-container');
  }

  _renderProperties() {
    super._renderProperties();
    this._renderWidget();
  }

  _remove() {
    this._removeWidget();
    super._remove();
  }

  setWidget(widget) {
    this.setProperty('widget', widget);
  }

  _renderWidget() {
    if (this.widget) {
      this.widget.render(this.$widgetContainer);
      this.widget.$container.addClass('widget');
      this.widget.pack();
    }
    this.$widgetContainer.setVisible(!!this.widget);
    if (!this.rendering) {
      this.position();
    }

    // Focus the widget
    // It is important that this happens after layouting and positioning, otherwise we'd focus an element
    // that is currently not on the screen. Which would cause the whole desktop to
    // be shifted for a few pixels.
    if (this.withFocusContext && this.widget) {
      this.session.focusManager.installFocusContext(this.$widgetContainer, this.initialFocus());
    }
  }

  _removeWidget() {
    if (this.widget) {
      this.session.focusManager.uninstallFocusContext(this.$widgetContainer);
      this.widget.remove();
    }
  }
}
