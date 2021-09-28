/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {InputFieldKeyStrokeContext, keys, keyStrokeModifier, Outline, scout, SearchOutlineLayout} from '../../index';
import $ from 'jquery';

export default class SearchOutline extends Outline {

  constructor() {
    super();
    this.hasText = false;
    this.$searchPanel = null;
    this.$clearIcon = null;
    this.$searchStatus = null;
    this.$queryField = null;
  }

  /**
   * @override Tree.js
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.searchFieldKeyStrokeContext = this._createKeyStrokeContextForSearchField();
  }

  _createKeyStrokeContextForSearchField() {
    let keyStrokeContext = new InputFieldKeyStrokeContext();
    keyStrokeContext.$scopeTarget = function() {
      return this.$searchPanel;
    }.bind(this);
    keyStrokeContext.$bindTarget = function() {
      return this.$queryField;
    }.bind(this);
    keyStrokeContext.registerStopPropagationKeys(keyStrokeModifier.NONE, [
      keys.ENTER, keys.BACKSPACE
    ]);
    return keyStrokeContext;
  }

  _render() {
    super._render();

    // Override layout
    this.htmlComp.setLayout(new SearchOutlineLayout(this));

    this.$container.addClass('search-outline');
    this.$searchPanel = this.$container.prependDiv('search-outline-panel');
    this.$queryField = this.$searchPanel.appendElement('<input>', 'search-outline-field')
      .on('input', this._createOnQueryFieldInputFunction().bind(this))
      .on('keypress', this._onQueryFieldKeyPress.bind(this));
    this.$clearIcon = this.$searchPanel.appendSpan('clear-icon unfocusable action text-field-icon')
      .on('mousedown', this._onClearIconMouseDown.bind(this));

    this.$searchStatus = this.$searchPanel.appendDiv('search-outline-status')
      .on('mousedown', this._onTitleMouseDown.bind(this));
    this.session.keyStrokeManager.installKeyStrokeContext(this.searchFieldKeyStrokeContext);
  }

  _remove() {
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.searchFieldKeyStrokeContext);
    this.$searchPanel.remove();
    super._remove();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderSearchQuery();
    this._renderSearchStatus();
    this._updateHasText();
  }

  _renderTitle() {
    super._renderTitle();
    // Move before search panel
    if (this.titleVisible) {
      this.$title.insertBefore(this.$searchPanel);
    }
  }

  _renderSearchQuery() {
    this.$queryField.val(this.searchQuery);
  }

  _renderSearchStatus() {
    let animate = this.rendered;

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
    this.$searchPanel.toggleClass('has-status', !!this.searchStatus);
  }

  focusQueryField() {
    this.validateFocus();
  }

  _triggerSearch() {
    this.trigger('search', {
      query: scout.nvl(this.searchQuery, '')
    });
  }

  _createOnQueryFieldInputFunction(event) {
    let debounceFunction = $.debounce(this._search.bind(this));
    return function(event) {
      this._updateHasText();
      // debounced search
      debounceFunction();
    };
  }

  _onClearIconMouseDown(event) {
    this.$queryField.val('');
    this._updateHasText();
    this._search();
    // focus field if x is pressed when the field does not have the focus
    this.$queryField.focus();
    // stay in field when x is pressed
    event.preventDefault();
  }

  _onQueryFieldKeyPress(event) {
    if (event.which === keys.ENTER) {
      this._setSearchQuery(this.$queryField.val());
      this._triggerSearch();
    }
  }

  _search(event) {
    // Don't send query if value did not change (may happen when _createOnQueryFieldInputFunction is executed after _onQueryFieldKeyPress)
    let searchQuery = this.$queryField.val();
    if (this.searchQuery !== searchQuery) {
      // Store locally so that the value persists when changing the outline without performing the search
      this._setSearchQuery(searchQuery);
      this._triggerSearch();
    }
  }

  _setSearchQuery(searchQuery) {
    this.searchQuery = searchQuery;
  }

  _updateHasText() {
    this.$queryField.toggleClass('has-text', !!this.$queryField.val());
  }

  /**
   * Focus and select content AFTER the search outline was rendered (and therefore the query field filled).
   *
   * @override Outline.js
   */
  validateFocus() {
    if (!this.rendered) {
      return;
    }
    let elementToFocus = this.$queryField[0];
    if (this.session.focusManager.requestFocus(elementToFocus)) {
      elementToFocus.select();
    }
  }
}
