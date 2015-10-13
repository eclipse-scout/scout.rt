scout.SearchOutline = function() {
  scout.SearchOutline.parent.call(this);
  this.$searchPanel;
  this.$searchStatus;
  this.$queryField;
};
scout.inherits(scout.SearchOutline, scout.Outline);

scout.SearchOutline.prototype._init = function(model) {
  scout.SearchOutline.parent.prototype._init.call(this, model);

  this.titleVisible = false;
};

/**
 * @override Tree.js
 */
scout.SearchOutline.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.SearchOutline.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  this.searchFieldKeyStrokeContext = this._createKeyStrokeContextForSearchField();
};

scout.SearchOutline.prototype._createKeyStrokeContextForSearchField = function() {
  var keyStrokeContext = new scout.KeyStrokeContext();
  keyStrokeContext.$scopeTarget = function() {
    return this.$searchPanel;
  }.bind(this);
  keyStrokeContext.$bindTarget = function() {
    return this.$queryField;
  }.bind(this);
  keyStrokeContext.registerStopPropagationKeys(scout.keyStrokeModifier.NONE, [
    scout.keys.ENTER, scout.keys.BACKSPACE
  ]);
  return keyStrokeContext;
};

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
  this.session.keyStrokeManager.installKeyStrokeContext(this.searchFieldKeyStrokeContext);
};

scout.SearchOutline.prototype._remove = function() {
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.searchFieldKeyStrokeContext);
  this.$searchPanel.remove();
  scout.SearchOutline.parent.prototype._remove.call(this);
};

scout.SearchOutline.prototype._renderProperties = function() {
  scout.SearchOutline.parent.prototype._renderProperties.call(this);
  this._renderSearchQuery(this.searchQuery);
  this._renderSearchStatus(this.searchStatus);
  this._renderRequestFocusQueryField();
};

scout.SearchOutline.prototype._renderSearchQuery = function(searchQuery) {
  this.$queryField.val(searchQuery);
};

scout.SearchOutline.prototype._renderSearchStatus = function(searchStatus) {
  this.$searchStatus.textOrNbsp(searchStatus);
};

scout.SearchOutline.prototype._renderRequestFocusQueryField = function() {
  this.validateFocus();
};

scout.SearchOutline.prototype._sendSearch = function() {
  this._send('search', {query: this.searchQuery});
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
 *
 * @override Outline.js
 */
scout.SearchOutline.prototype.validateFocus = function() {
  var elementToFocus = this.$queryField[0];
  if (this.session.focusManager.requestFocus(elementToFocus)) {
    elementToFocus.select();
  }
};
