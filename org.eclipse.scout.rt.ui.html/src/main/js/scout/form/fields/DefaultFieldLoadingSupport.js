scout.DefaultFieldLoadingSupport = function(field) {
  this.field = field;
};

scout.DefaultFieldLoadingSupport.prototype.renderLoading = function() {
  if (!this.field) {
    return;
  }
  this.field.$container.toggleClass('loading', this.field.loading);
  if (this.field.loading && !this._$loadingIndicator) {
    // Create loading indicator
    this._$loadingIndicator = $.makeDiv('loading-indicator').appendTo(this.field.$container);
  } else if (!this.field.loading && this._$loadingIndicator) {
    // Remove loading indicator
    this._$loadingIndicator.remove();
    this._$loadingIndicator = null;
    //if initial loading field is not yet layoutet -> do layout
    this.field.invalidateLayoutTree();
  }
};
