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

/**
 * @param {object} options The following properties are supported:
 *  widget                  Widget that created the loading support
 *  [$container]            jQuery element that should be hidden when the widget is in loading state. If this property is not
 *                          set the $container of the widget is used by default (see _ensureContainer).
 *  [loadingIndicatorDelay] if not set: 250 ms
 */
scout.LoadingSupport = function(options) {
  scout.assertParameter('widget', options.widget);

  this.widget = options.widget;
  this.options$Container = options.$container;
  this.loadingIndicatorDelay = scout.nvl(options.loadingIndicatorDelay, 250); // ms

  this._$loadingIndicator = null;
  this._loadingIndicatorTimeoutId = null;
};

scout.LoadingSupport.prototype.setLoadingIndicatorDelay = function(loadingIndicatorDelay) {
  this.loadingIndicatorDelay = loadingIndicatorDelay;
};

scout.LoadingSupport.prototype._ensure$Container = function() {
  if (scout.objects.isFunction(this.options$Container)) {
    // resolve function provided by options.$container that returns a jQuery element
    this.$container = this.options$Container();
  } else if (this.options$Container) {
    // use jQuery element provided by options.$container
    this.$container = this.options$Container;
  } else {
    // default: when no options.$container is not set, use jQuery element of widget
    this.$container = this.widget.$container;
  }
};

scout.LoadingSupport.prototype.renderLoading = function() {
  // Clear any pending loading function
  clearTimeout(this._loadingIndicatorTimeoutId);
  this._ensure$Container();

  if (this.widget.loading) {
    // add loading indicator
    if (this.loadingIndicatorDelay) {
      this._loadingIndicatorTimeoutId = setTimeout(
          this._renderLoadingIndicator.bind(this), this.loadingIndicatorDelay);
    } else {
      this._renderLoadingIndicator();
    }
  } else {
    // remove loading indicator
    this._removeLoadingIndicator();
  }
};

scout.LoadingSupport.prototype._renderLoadingIndicator = function() {
  if (this._$loadingIndicator || !this.widget.rendered && !this.widget.rendering) {
    return;
  }

  // Hide widget content
  this.$container.addClass('loading');
  // Create loading indicator
  this._$loadingIndicator = this.$container.appendDiv('loading-indicator');
};

scout.LoadingSupport.prototype._removeLoadingIndicator = function() {
  if (!this._$loadingIndicator) {
    return;
  }

  this._$loadingIndicator.fadeOutAndRemove(function() {
    this._$loadingIndicator = null;
    if (this.widget.rendered) {
      // Show widget's content (layout if necessary)
      this.$container.removeClass('loading');
      this.widget.invalidateLayoutTree();
    }
  }.bind(this));
};
