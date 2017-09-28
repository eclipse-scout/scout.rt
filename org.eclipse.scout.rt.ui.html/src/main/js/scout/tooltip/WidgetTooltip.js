/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.WidgetTooltip = function() {
  scout.WidgetTooltip.parent.call(this);

  this.$widgetContainer = null;
  this.widget = null;
  this._addWidgetProperties(['widget']);

  // Default interceptor that stops the propagation for all key strokes except ESCAPE and ENTER.
  // Otherwise, the tooltip would be destroyed for all key strokes that bubble up to the
  // root (see global document listener in Tooltip.js).
  this.keyStrokeStopPropagationInterceptor = function(event) {
    if (scout.isOneOf(event.which, scout.keys.ESC, scout.keys.ENTER)) {
      return;
    }
    event.stopPropagation();
  };

  this.withFocusContext = true;
  this.initialFocus = function() {
    return scout.focusRule.AUTO;
  };
  this.focusableContainer = false;
};
scout.inherits(scout.WidgetTooltip, scout.Tooltip);

scout.WidgetTooltip.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

scout.WidgetTooltip.prototype._initKeyStrokeContext = function() {
  scout.WidgetTooltip.parent.prototype._initKeyStrokeContext.call(this);
  if (this.keyStrokeStopPropagationInterceptor) {
    this.keyStrokeContext.registerStopPropagationInterceptor(this.keyStrokeStopPropagationInterceptor);
  }
};

scout.WidgetTooltip.prototype._render = function() {
  scout.WidgetTooltip.parent.prototype._render.call(this);
  this.$container.addClass('widget-tooltip');
  this.$widgetContainer = this.$container.appendDiv('tooltip-widget-container');
};

scout.WidgetTooltip.prototype._renderProperties = function() {
  scout.WidgetTooltip.parent.prototype._renderProperties.call(this);
  this._renderWidget();
};

scout.WidgetTooltip.prototype.setWidget = function(widget) {
  this.setProperty('widget', widget);
};

scout.WidgetTooltip.prototype._removeWidget = function() {
  if (this.widget) {
    this.session.focusManager.uninstallFocusContext(this.$widgetContainer);
    this.widget.remove();
  }
};

scout.WidgetTooltip.prototype._renderWidget = function() {
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
};
