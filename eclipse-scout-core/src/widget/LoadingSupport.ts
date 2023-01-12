/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LoadingSupportOptions, scout, WidgetSupport} from '../index';

export class LoadingSupport extends WidgetSupport {
  loadingIndicatorDelay: number;
  protected _$loadingIndicator: JQuery;
  protected _loadingIndicatorTimeoutId: number;

  /**
   * @param options a mandatory options object
   */
  constructor(options: LoadingSupportOptions) {
    super(options);
    this.loadingIndicatorDelay = scout.nvl(options.loadingIndicatorDelay, 250); // ms

    this._$loadingIndicator = null;
    this._loadingIndicatorTimeoutId = null;
  }

  setLoadingIndicatorDelay(loadingIndicatorDelay: number) {
    this.loadingIndicatorDelay = loadingIndicatorDelay;
  }

  protected override _ensure$Container() {
    if (typeof this.options$Container === 'function') {
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

  protected _renderLoadingIndicator() {
    if (this._$loadingIndicator || !this.widget.rendered && !this.widget.rendering) {
      return;
    }

    // Hide widget content
    this.$container.addClass('loading');
    // Create loading indicator
    this._$loadingIndicator = this.$container.appendDiv('loading-indicator');
  }

  protected _removeLoadingIndicator() {
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
