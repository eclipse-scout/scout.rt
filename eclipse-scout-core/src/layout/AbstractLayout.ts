/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Dimension, graphics, HtmlComponent, HtmlCompPrefSizeOptions} from '../index';
import $ from 'jquery';

/**
 * A layout is responsible to layout the children of a container and is able to return its preferred size.
 */
export class AbstractLayout {
  animateClasses: string[];
  cssClass?: string;

  constructor() {
    this.animateClasses = [];
  }

  /**
   * Called when layout is invalidated. An implementation should delete cached layout-information when it is invalidated.
   *
   * May be implemented by a sub-class.
   *
   * @param htmlSource The component the invalidation originated from.
   *        Is always set if the invalidation is triggered by using {@link HtmlComponent.invalidateLayoutTree}, may be undefined otherwise.
   */
  invalidate(htmlSource?: HtmlComponent) {
    // nop
  }

  /**
   * Layouts children of the given $container, according to the implemented layout algorithm.
   * The implementation should call {@link HtmlComponent.setSize} or {@link HtmlComponent.setBounds} on its children which will validate their layout.
   */
  layout($container: JQuery) {
    // nop
  }

  /**
   * Reverts the adjustments made by {@link HtmlComponent#_adjustSizeHintsForPrefSize} without the margin.
   * More concrete: it adds border and padding to the hints again.
   */
  protected _revertSizeHintsAdjustments($container: JQuery, options: HtmlCompPrefSizeOptions) {
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
   */
  preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
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
