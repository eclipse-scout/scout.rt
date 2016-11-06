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
 * @param options The following properties are supported:
 *  [options] mandatory
 *  [containerProperty] optional, if not set: '$container' is used. This property points to the JQuery element that
 *      should be hidden when the field is in loading state. We cannot reference the property in the Ctor of this
 *      class, because the property is not set until the render() method runs.
 *  [loadingIndicatorDelay] optional, if not set: 250 ms
 */
scout.LoadingSupport = function(options) {
  if (!options.widget) {
    throw new Error('Option \'widget\' not set');
  }
  this.widget = options.widget;
  this.$container = options.$container || function() {
    return this.widget.$container;
  }.bind(this);
  this.loadingIndicatorDelay = scout.nvl(options.loadingIndicatorDelay, 250); // ms

  this._loadingIndicatorTimeoutId;
};

scout.LoadingSupport.prototype.setLoadingIndicatorDelay = function(loadingIndicatorDelay) {
  this.loadingIndicatorDelay = loadingIndicatorDelay;
};

scout.LoadingSupport.prototype.renderLoading = function() {
  // Clear any pending loading function
  clearTimeout(this._loadingIndicatorTimeoutId);

  if (!this.widget) {
    return;
  }

  if (this.widget.loading && !this._$loadingIndicator) {
    // --- 1. not loading -> loading ---

    var renderLoading = function() {
      if (this.widget.rendered) {
        // Hide widget content
        this.$container().addClass('loading');
        // Create loading indicator
        this._$loadingIndicator = this.$container().appendDiv('loading-indicator');
      }
    }.bind(this);

    if (this.loadingIndicatorDelay) {
      this._loadingIndicatorTimeoutId = setTimeout(renderLoading, this.loadingIndicatorDelay);
    } else {
      renderLoading();
    }

  } else if (!this.widget.loading && this._$loadingIndicator) {
    // --- 2. loading -> not loading ---

    // Remove loading indicator
    this._$loadingIndicator.fadeOutAndRemove(function() {
      this._$loadingIndicator = null;
      if (this.widget.rendered) {
        // Show widget's content (layout if necessary)
        this.$container().removeClass('loading');
        this.widget.invalidateLayoutTree();
      }
    }.bind(this));
  }
};
