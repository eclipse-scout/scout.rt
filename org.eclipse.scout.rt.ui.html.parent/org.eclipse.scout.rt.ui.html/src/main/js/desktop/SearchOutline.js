scout.SearchOutline = function() {
  scout.SearchOutline.parent.call(this);
};
scout.inherits(scout.SearchOutline, scout.Outline);

scout.SearchOutline.prototype._render = function($parent) {
  scout.SearchOutline.parent.prototype._render.call(this, $parent);

  this.$searchStatus = $parent.prependDiv('search-status');
};

scout.SearchOutline.prototype._remove = function() {
  scout.SearchOutline.parent.prototype._remove.call(this);

  this.$searchStatus.remove();
};

scout.SearchOutline.prototype._renderProperties = function() {
  scout.SearchOutline.parent.prototype._renderProperties.call(this);

  this._renderSearchQuery(this.searchQuery);
  this._renderSearchStatus(this.searchStatus);
};

scout.SearchOutline.prototype._renderSearchQuery = function(searchQuery) {
  this.session.desktop.navigation.renderSearchQuery(searchQuery);
};

scout.SearchOutline.prototype._renderSearchStatus = function(searchStatus) {
  this.$searchStatus.text(searchStatus);
};

scout.SearchOutline.prototype.performSearch  = function(query) {
  this.session.send('search', this.id, {
    'query': this.searchQuery
  });
};
