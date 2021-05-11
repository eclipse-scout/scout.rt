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
import {Dimension, graphics, HtmlComponent} from '../index';
import $ from 'jquery';

/**
 * Abstract layout class with functions used by all layout algorithms.
 * Subclasses of AbstractLayout.js must implement the following functions:
 * - layout
 * - preferredLayoutSize
 */
export default class AbstractLayout {

  constructor() {
    this.animateClasses = [];
  }

  /**
   * Called when layout is invalidated. An implementation should delete cached layout-information
   * when it is invalidated.
   *
   * May be implemented by sub-class.
   */
  invalidate() { //
  }

  /**
   * Layouts children of the given $container, according to the implemented layout algorithm.
   * The implementation should call setSize or setBounds on its children.
   *
   * Must be implemented by sub-class.
   */
  layout($container) { //
  }

  /**
   * Reverts the adjustments made by {@link HtmlComponent#_adjustSizeHintsForPrefSize} without the margin.
   * More concrete: it adds border and padding to the hints again.
   */
  _revertSizeHintsAdjustments($container, options) {
    let htmlContainer = HtmlComponent.get($container);
    if (options.widthHint) {
      options.widthHint += htmlContainer.insets().horizontal();
    }
    if (options.heightHint) {
      options.heightHint += htmlContainer.insets().vertical();
    }
  }

  /**
   * Returns the preferred size of the given $container.
   *
   * @return Dimension preferred size
   */
  preferredLayoutSize($container, options) {
    options = $.extend({}, options);
    if (this.animateClasses.length > 0) {
      options.animateClasses = this.animateClasses;
    }
    // Insets have been removed automatically by the html component with the assumption that the layout will pass it to its child elements.
    // Since this is not the case in this generic layout the insets have to be added again, otherwise the sizes used to measure would be too small.
    this._revertSizeHintsAdjustments($container, options);
    return graphics.prefSize($container, options);
  }
}
