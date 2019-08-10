/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.GlassPane = function() {
  scout.GlassPane.parent.call(this);
};
scout.inherits(scout.GlassPane, scout.Widget);

scout.GlassPane.prototype._render = function() {
  this.$container = this.$parent
    .appendDiv('glasspane')
    .on('mousedown', this._onMouseDown.bind(this));

  // This is required in touch mode, because FastClick messes up the order
  // of mouse/click events which is especially important for TouchPopups.
  if (scout.device.supportsTouch()) {
    this.$container.addClass('needsclick');
  }

  this.$parent.addClass('glasspane-parent');
  var cssPosition = this.$parent.css('position');
  if (!scout.isOneOf(cssPosition, 'relative', 'absolute')) {
    this.$parent.css('position', 'relative');
  }

  // Register 'glassPaneTarget' in focus manager.
  this.session.focusManager.registerGlassPaneTarget(this.$parent);
};

scout.GlassPane.prototype._remove = function() {
  this.$parent.removeClass('glasspane-parent');
  this.session.focusManager.unregisterGlassPaneTarget(this.$parent);
  scout.GlassPane.parent.prototype._remove.call(this);
};

scout.GlassPane.prototype._onMouseDown = function(event) {
  // Won't be executed if pointer events is set to none. But acts as safety net if pointer events are not supported or even removed by the user
  $.suppressEvent(event);
};
