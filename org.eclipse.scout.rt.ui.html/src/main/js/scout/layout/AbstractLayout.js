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
/**
 * Abstract layout class with functions used by all layout algorithms.
 * Subclasses of AbstactLayout.js must implement the following functions:
 * - layout
 * - preferredLayoutSize
 */
scout.AbstractLayout = function() {
  this.animateClasses = [];
};

/**
 * Called when layout is invalidated. An implementation should delete cached layout-information
 * when it is invalidated.
 *
 * May be implemented by sub-class.
 */
scout.AbstractLayout.prototype.invalidate = function() { //
};

/**
 * Layouts children of the given $container, according to the implemented layout algorithm.
 * The implementation should call setSize or setBounds on its children.
 *
 * Must be implemented by sub-class.
 */
scout.AbstractLayout.prototype.layout = function($container) { //
};

/**
 * Reverts the adjustments made by {@link scout.HtmlComponent#_adjustSizeHintsForPrefSize} without the margin.
 * More concrete: it adds border and padding to the hints again.
 */
scout.AbstractLayout.prototype._revertSizeHintsAdjustments = function($container, options) {
  var htmlContainer = scout.HtmlComponent.get($container);
  if (options.widthHint) {
    options.widthHint += htmlContainer.insets().horizontal();
  }
  if (options.heightHint) {
    options.heightHint += htmlContainer.insets().vertical();
  }
};

/**
 * Returns the preferred size of the given $container.
 *
 * @return scout.Dimension preferred size
 */
scout.AbstractLayout.prototype.preferredLayoutSize = function($container, options) {
  options = $.extend({}, options);
  if (this.animateClasses.length > 0) {
    options.animateClasses = this.animateClasses;
  }
  // Insets have been removed automatically by the html component with the assumption that the layout will pass it to its child elements.
  // Since this is not the case in this generic layout the insets have to be added again, otherwise the sizes used to measure would be too small.
  this._revertSizeHintsAdjustments($container, options);
  return scout.graphics.prefSize($container, options);
};
