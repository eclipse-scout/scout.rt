/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, InputFieldKeyStrokeContext, keys, KeyStrokeContext, keyStrokeModifier, Outline, scout, SearchOutlineEventMap, SearchOutlineLayout, SearchOutlineModel} from '../../index';
import $ from 'jquery';

export class SearchOutline extends Outline implements SearchOutlineModel {
  declare model: SearchOutlineModel;
  declare eventMap: SearchOutlineEventMap;
  declare self: SearchOutline;

  hasText: boolean;
  searchQuery: string;
  searchStatus: string;
  searchFieldKeyStrokeContext: KeyStrokeContext;
  $searchPanel: JQuery;
  $clearIcon: JQuery;
  $searchStatus: JQuery;
  $queryField: JQuery<HTMLInputElement>;

  constructor() {
    super();
    this.hasText = false;
    this.$searchPanel = null;
    this.$clearIcon = null;
    this.$searchStatus = null;
    this.$queryField = null;
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.searchFieldKeyStrokeContext = this._createKeyStrokeContextForSearchField();
  }

  protected _createKeyStrokeContextForSearchField(): KeyStrokeContext {
    let keyStrokeContext = new InputFieldKeyStrokeContext();
    keyStrokeContext.$scopeTarget = () => this.$searchPanel;
    keyStrokeContext.$bindTarget = () => this.$queryField;
    keyStrokeContext.registerStopPropagationKeys(keyStrokeModifier.NONE, [keys.ENTER, keys.BACKSPACE]);
    return keyStrokeContext;
  }

  protected override _render() {
    super._render();

    // Override layout
    this.htmlComp.setLayout(new SearchOutlineLayout(this));

    this.$container.addClass('search-outline');
    this.$searchPanel = this.$container.prependDiv('search-outline-panel');
    this.$queryField = this.$searchPanel.appendElement('<input>', 'search-outline-field')
      .on('input', this._createOnQueryFieldInputFunction().bind(this))
      .on('keypress', this._onQueryFieldKeyPress.bind(this)) as JQuery<HTMLInputElement>;
    this.$clearIcon = this.$searchPanel.appendSpan('clear-icon unfocusable action text-field-icon')
      .on('mousedown', this._onClearIconMouseDown.bind(this));

    this.$searchStatus = this.$searchPanel.appendDiv('search-outline-status')
      .on('mousedown', this._onTitleMouseDown.bind(this));
    this.session.keyStrokeManager.installKeyStrokeContext(this.searchFieldKeyStrokeContext);
  }

  protected override _remove() {
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.searchFieldKeyStrokeContext);
    this.$searchPanel.remove();
    super._remove();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderSearchQuery();
    this._renderSearchStatus();
    this._updateHasText();
  }

  protected override _renderTitle() {
    super._renderTitle();
    // Move before search panel
    if (this.titleVisible) {
      this.$title.insertBefore(this.$searchPanel);
      aria.linkElementWithLabel(this.$queryField, this.$title);
    }
  }

  protected _renderSearchQuery() {
    this.$queryField.val(this.searchQuery);
  }

  protected _renderSearchStatus() {
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

  protected _triggerSearch() {
    this.trigger('search', {
      query: scout.nvl(this.searchQuery, '')
    });
  }

  protected _createOnQueryFieldInputFunction(): (event: JQuery.TriggeredEvent) => void {
    let debounceFunction = $.debounce(this._search.bind(this));
    return function(event) {
      this._updateHasText();
      // debounced search
      debounceFunction();
    };
  }

  protected _onClearIconMouseDown(event: JQuery.MouseDownEvent) {
    this.$queryField.val('');
    this._updateHasText();
    this._search();
    // focus field if x is pressed when the field does not have the focus
    this.$queryField.focus();
    // stay in field when x is pressed
    event.preventDefault();
  }

  protected _onQueryFieldKeyPress(event: JQuery.KeyPressEvent) {
    if (event.which === keys.ENTER) {
      this._setSearchQuery(this.$queryField.val() as string);
      this._triggerSearch();
    }
  }

  protected _search() {
    // Don't send query if value did not change (may happen when _createOnQueryFieldInputFunction is executed after _onQueryFieldKeyPress)
    let searchQuery = this.$queryField.val() as string;
    if (this.searchQuery !== searchQuery) {
      // Store locally so that the value persists when changing the outline without performing the search
      this._setSearchQuery(searchQuery);
      this._triggerSearch();
    }
  }

  protected _setSearchQuery(searchQuery: string) {
    this.searchQuery = searchQuery;
  }

  protected _updateHasText() {
    this.$queryField.toggleClass('has-text', !!this.$queryField.val());
  }

  /**
   * Focus and select content AFTER the search outline was rendered (and therefore the query field filled).
   */
  override validateFocus() {
    if (!this.rendered) {
      return;
    }
    let elementToFocus = this.$queryField[0];
    if (this.session.focusManager.requestFocus(elementToFocus)) {
      elementToFocus.select();
    }
  }
}
