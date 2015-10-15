scout.DefaultFieldLoadingSupport = function(field, loadingIndicatorDelay) {
  this.field = field;
  this.loadingIndicatorDelay = scout.helpers.nvl(loadingIndicatorDelay, 250); // ms

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
      // Hide field content
      this.field.$container.addClass('loading');
      // Create loading indicator
      this._$loadingIndicator = $.makeDiv('loading-indicator').appendTo(this.field.$container);
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
      // Show field's content (layout if necessary)
      this.field.$container.removeClass('loading');
      this.field.invalidateLayoutTree();
    }.bind(this));
  }
};
