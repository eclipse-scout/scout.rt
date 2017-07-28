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
scout.SearchOutline = function() {
  scout.SearchOutline.parent.call(this);
  this.hasText = false;
  this.$searchPanel;
  this.$clearIcon;
  this.$searchStatus;
  this.$queryField;
};
scout.inherits(scout.SearchOutline, scout.Outline);

/**
 * @override Tree.js
 */
scout.SearchOutline.prototype._initKeyStrokeContext = function() {
  scout.SearchOutline.parent.prototype._initKeyStrokeContext.call(this);

  this.searchFieldKeyStrokeContext = this._createKeyStrokeContextForSearchField();
};

scout.SearchOutline.prototype._createKeyStrokeContextForSearchField = function() {
  var keyStrokeContext = new scout.InputFieldKeyStrokeContext();
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

scout.SearchOutline.prototype._render = function() {
  scout.SearchOutline.parent.prototype._render.call(this);

  // Override layout
  this.htmlComp.setLayout(new scout.SearchOutlineLayout(this));

  this.$container.addClass('search-outline');
  this.$searchPanel = this.$container.prependDiv('search-outline-panel');
  this.$queryField = this.$searchPanel.appendElement('<input>', 'search-outline-field')
    .on('input', this._createOnQueryFieldInputFunction().bind(this))
    .on('keypress', this._onQueryFieldKeyPress.bind(this));
  this.$clearIcon = this.$searchPanel.appendSpan('delete-icon unfocusable needsclick')
    .on('mousedown', this._onClearIconMouseDown.bind(this));

  this.$searchStatus = this.$searchPanel.appendDiv('search-outline-status')
    .on('click', this._onTitleClick.bind(this));
  this.session.keyStrokeManager.installKeyStrokeContext(this.searchFieldKeyStrokeContext);
};

scout.SearchOutline.prototype._remove = function() {
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.searchFieldKeyStrokeContext);
  this.$searchPanel.remove();
  scout.SearchOutline.parent.prototype._remove.call(this);
};

scout.SearchOutline.prototype._renderProperties = function() {
  scout.SearchOutline.parent.prototype._renderProperties.call(this);
  this._renderSearchQuery();
  this._renderSearchStatus();
  this._updateHasText();
};

scout.SearchOutline.prototype._renderTitle = function() {
  scout.SearchOutline.parent.prototype._renderTitle.call(this);
  // Move before search panel
  if (this.titleVisible) {
    this.$title.insertBefore(this.$searchPanel);
  }
};

scout.SearchOutline.prototype._renderSearchQuery = function() {
  this.$queryField.val(this.searchQuery);
};

scout.SearchOutline.prototype._renderSearchStatus = function() {
  var animate = this.rendered;

  if (this.searchStatus && !this.$searchStatus.isVisible()) {
    if (animate) {
      this.$searchStatus.slideDown({
        duration: 200,
        progress: this.revalidateLayout.bind(this)
      });
    } else {
      this.$searchStatus.show();
    }
  } else if (!this.searchStatus && this.$searchStatus.isVisible()) {
    if (animate) {
      this.$searchStatus.slideUp({
        duration: 200,
        progress: this.revalidateLayout.bind(this)
      });
    } else {
      this.$searchStatus.hide();
    }
  }
  this.$searchStatus.textOrNbsp(this.searchStatus);
};

/**
 * @deprecated use #focusQueryField instead, will be removed with 7.1
 */
scout.SearchOutline.prototype.requestFocusQueryField = function() {
  this.focusQueryField();
};

scout.SearchOutline.prototype.focusQueryField = function() {
  this.validateFocus();
};

scout.SearchOutline.prototype._triggerSearch = function() {
  this.trigger('search', {
    query: scout.nvl(this.searchQuery, '')
  });
};

scout.SearchOutline.prototype._createOnQueryFieldInputFunction = function(event) {
  var debounceFunction = $.debounce(this._search.bind(this));
  var fn = function(event) {
    this._updateHasText();
    // debounced search
    debounceFunction();
  };
  return fn;
};

scout.SearchOutline.prototype._onClearIconMouseDown = function(event) {
  this.$queryField.val('');
  this._updateHasText();
  this._search();
  event.preventDefault();

};

scout.SearchOutline.prototype._onQueryFieldKeyPress = function(event) {
  if (event.which === scout.keys.ENTER) {
    this._setSearchQuery(this.$queryField.val());
    this._triggerSearch();
  }
};

scout.SearchOutline.prototype._search = function(event) {
  // Don't send query if value did not change (may happen when _createOnQueryFieldInputFunction is executed after _onQueryFieldKeyPress)
  var searchQuery = this.$queryField.val();
  if (this.searchQuery !== searchQuery) {
    // Store locally so that the value persists when changing the outline without performing the search
    this._setSearchQuery(searchQuery);
    this._triggerSearch();
  }
};

scout.SearchOutline.prototype._setSearchQuery = function(searchQuery) {
  this.searchQuery = searchQuery;
};

scout.SearchOutline.prototype._updateHasText = function() {
  this.$queryField.toggleClass('has-text', !!this.$queryField.val());
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
