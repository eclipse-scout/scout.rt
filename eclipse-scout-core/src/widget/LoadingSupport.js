/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {objects, scout} from '../index';
import WidgetSupport from './WidgetSupport';

export default class LoadingSupport extends WidgetSupport {

  /**
   * @typedef {WidgetSupportOptions} LoadingSupportOptions
   * @property {number} [loadingIndicatorDelay] if not set: 250 ms
   */

  /**
   * @param {LoadingSupportOptions} options a mandatory options object
   */
  constructor(options) {
    super(options);
    this.loadingIndicatorDelay = scout.nvl(options.loadingIndicatorDelay, 250); // ms

    this._$loadingIndicator = null;
    this._loadingIndicatorTimeoutId = null;
  }

  setLoadingIndicatorDelay(loadingIndicatorDelay) {
    this.loadingIndicatorDelay = loadingIndicatorDelay;
  }

  _ensure$Container() {
    if (objects.isFunction(this.options$Container)) {
      // resolve function provided by options.$container that returns a jQuery element
      this.$container = this.options$Container();
    } else if (this.options$Container) {
      // use jQuery element provided by options.$container
      this.$container = this.options$Container;
    } else {
      // default: when no options.$container is not set, use jQuery element of widget
      this.$container = this.widget.$container;
    }
  }

  renderLoading() {
    // Clear any pending loading function
    clearTimeout(this._loadingIndicatorTimeoutId);
    this._ensure$Container();

    if (this.widget.isLoading()) {
      // add loading indicator
      if (this.loadingIndicatorDelay && !this.widget.rendering) {
        this._loadingIndicatorTimeoutId = setTimeout(
          this._renderLoadingIndicator.bind(this), this.loadingIndicatorDelay);
      } else {
        this._renderLoadingIndicator();
      }
    } else {
      // remove loading indicator
      this._removeLoadingIndicator();
    }
  }

  _renderLoadingIndicator() {
    if (this._$loadingIndicator || !this.widget.rendered && !this.widget.rendering) {
      return;
    }

    // Hide widget content
    this.$container.addClass('loading');
    // Create loading indicator
    this._$loadingIndicator = this.$container.appendDiv('loading-indicator');
  }

  _removeLoadingIndicator() {
    if (!this._$loadingIndicator) {
      return;
    }

    this._$loadingIndicator.css('opacity', this._$loadingIndicator.css('opacity'));
    this._$loadingIndicator.addClass('animate-remove');
    this._$loadingIndicator.oneAnimationEnd(() => {
      this.remove();
      if (this.widget.rendered) {
        // Show widget's content (layout if necessary)
        this.$container.removeClass('loading');
        this.widget.invalidateLayoutTree();
      }
    });
  }

  remove() {
    if (this._$loadingIndicator) {
      this._$loadingIndicator.remove();
      this._$loadingIndicator = null;
    }
  }
}
