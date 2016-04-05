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
 *  [field] mandatory
 *  [containerProperty] optional, if not set: '$container' is used. This property points to the JQuery element that
 *      should be hidden when the field is in loading state. We cannot reference the property in the Ctor of this
 *      class, because the property is not set until the render() method runs.
 *  [loadingIndicatorDelay] optional, if not set: 250 ms
 */
scout.DefaultFieldLoadingSupport = function(options) {
  if (!options.field) {
    throw new Error('Option \'field\' not set');
  }
  this.field = options.field;
  this.$container = options.$container || function() {
    return this.field.$container;
  }.bind(this);
  this.loadingIndicatorDelay = scout.nvl(options.loadingIndicatorDelay, 250); // ms

  this._loadingIndicatorTimeoutId;
};

scout.DefaultFieldLoadingSupport.prototype.renderLoading = function() {
  // Clear any pending loading function
  clearTimeout(this._loadingIndicatorTimeoutId);

  if (!this.field) {
    return;
  }

  if (this.field.loading && !this._$loadingIndicator) {
    // --- 1. not loading -> loading ---

    var renderLoading = function() {
      if (this.field.rendered) {
        // Hide field content
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

  } else if (!this.field.loading && this._$loadingIndicator) {
    // --- 2. loading -> not loading ---

    // Remove loading indicator
    this._$loadingIndicator.fadeOutAndRemove(function() {
      this._$loadingIndicator = null;
      if (this.field.rendered) {
        // Show field's content (layout if necessary)
        this.$container().removeClass('loading');
        this.field.invalidateLayoutTree();
      }
    }.bind(this));
  }
};
