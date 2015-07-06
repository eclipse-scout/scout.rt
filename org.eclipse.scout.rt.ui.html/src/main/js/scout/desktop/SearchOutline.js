scout.SearchOutline = function() {
  scout.SearchOutline.parent.call(this);
  this.$searchPanel;
  this.$searchStatus;
  this.$queryField;
  this.searchFieldKeyStrokeAdapter;
};
scout.inherits(scout.SearchOutline, scout.Outline);

scout.SearchOutline.prototype._render = function($parent) {
  scout.SearchOutline.parent.prototype._render.call(this, $parent);
  // insert the search-panel _above_ the $container
  this.$searchPanel = $parent.prependDiv('search-panel');
  this.$queryField = $('<input>')
    .addClass('search-field')
    .placeholder(this.session.text('ui.SearchFor_'))
    .on('input', this._onQueryFieldInput.bind(this))
    .on('keypress', this._onQueryFieldKeyPress.bind(this))
    .appendTo(this.$searchPanel);
  this.$searchStatus = this.$searchPanel.appendDiv('search-status');

  // add keystroke adapter for search field
  if (!this.searchFieldKeyStrokeAdapter) {
    this.searchFieldKeyStrokeAdapter = new scout.SearchFieldKeyStrokeAdapter(this);
  }
  // reinstall
  scout.keyStrokeManager.uninstallAdapter(this.searchFieldKeyStrokeAdapter);
  scout.keyStrokeManager.installAdapter(this.$queryField, this.searchFieldKeyStrokeAdapter);
};

scout.SearchOutline.prototype._remove = function() {
  this.$searchPanel.remove();
  scout.SearchOutline.parent.prototype._remove.call(this);
};

scout.SearchOutline.prototype._renderProperties = function() {
  scout.SearchOutline.parent.prototype._renderProperties.call(this);
  this._renderSearchQuery(this.searchQuery);
  this._renderSearchStatus(this.searchStatus);
};

scout.SearchOutline.prototype._renderSearchQuery = function(searchQuery) {
  this.$queryField.val(searchQuery);
};

scout.SearchOutline.prototype._renderSearchStatus = function(searchStatus) {
  this.$searchStatus.textOrNbsp(searchStatus);
};

scout.SearchOutline.prototype._sendSearch  = function() {
  this.session.send(this.id, 'search', {
    query: this.searchQuery
  });
};

scout.SearchOutline.prototype._onQueryFieldInput = function(event) {
  // Store locally so that the value persists when changing the outline without performing the search
  this.searchQuery = this.$queryField.val();
};

scout.SearchOutline.prototype._onQueryFieldKeyPress = function(event) {
  if (event.which === scout.keys.ENTER) {
    this._sendSearch();
  }
};

/**
 * Focus and select content AFTER the search outline was rendered (and therefore the query field filled).
 * @override Outline.js
 */
scout.SearchOutline.prototype.validateFocus = function() {
  this.$queryField.focus();
  this.$queryField[0].select();
};
