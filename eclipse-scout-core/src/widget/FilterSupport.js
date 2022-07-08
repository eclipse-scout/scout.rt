/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, FormField, HAlign, keys, KeyStroke, objects, scout, strings, styles, ValueField, WidgetSupport} from '../index';
import FocusFilterFieldKeyStroke from '../keystroke/FocusFilterFieldKeyStroke';

export default class FilterSupport extends WidgetSupport {
  /**
   * @typedef {WidgetSupportOptions} FilterSupportOptions
   * @property {function} filterElements Filter all elements.
   * @property {function} getElementsForFiltering Get all elements to which the filters should be applied.
   * @property {function} getElementText Get text of an element.
   * @property {function} createTextFilter Create a text filter.
   * @property {function} updateTextFilterText Update the text on the filter, this is mandatory if createTextFilter is set.
   */

  /**
   * @typedef Filter
   * @property {function} accept A function that returns true or false, whether the filter accepts the element or not.
   */

  /**
   * @typedef FilterResult
   * @property {object[]} newlyHidden An array of the newly hidden elements.
   * @property {object[]} newlyShown An array of the newly shown elements.
   */

  /**
   * @typedef SetFiltersResult
   * @property {Filter[]} filtersAdded An array of the filters added.
   * @property {Filter[]} filtersRemoved An array of the filters removed.
   */

  /**
   * @param {FilterSupportOptions} options a mandatory options object
   */
  constructor(options) {
    super(options);

    if (options.filterElements) {
      this._filterElements = options.filterElements;
    } else {
      this._filterElements = this._filter.bind(this);
      scout.assertParameter('getElementsForFiltering', options.getElementsForFiltering);
      this._getElementsForFiltering = options.getElementsForFiltering;
    }
    this._getElementText = options.getElementText || (element => $(element).text());

    if (options.createTextFilter) {
      scout.assertParameter('updateTextFilterText', options.updateTextFilterText);
      this._createTextFilter = options.createTextFilter;
      this._updateTextFilterText = options.updateTextFilterText;
    } else {
      this._createTextFilter = this._createDefaultTextFilter.bind(this);
      this._updateTextFilterText = this._updateDefaultTextFilterText.bind(this);
    }

    this._filterField = null;

    this._filterFieldDisplayTextChangedHandler = this._onFilterFieldDisplayTextChanged.bind(this);
    this._focusInHandler = this._onFocusIn.bind(this);
    this._focusOutHandler = this._onFocusOut.bind(this);

    this._focusFilterFieldKeyStroke = null;
    this._cancelFilterFieldKeyStroke = null;
    this._exitFilterFieldKeyStroke = null;

    this._textFilter = null;
  }

  _createDefaultTextFilter() {
    return {
      acceptedText: null,
      accept: element => {
        if (strings.empty(this._textFilter.acceptedText) || !this.widget.isTextFilterFieldVisible()) {
          return true;
        }
        let text = this._getElementText(element);
        if (strings.empty(text)) {
          return false;
        }
        return strings.contains(text.toLowerCase(), this._textFilter.acceptedText.toLowerCase());
      }
    };
  }

  _updateDefaultTextFilterText(filter, text) {
    if (objects.equals(filter.acceptedText, text)) {
      return false;
    }
    filter.acceptedText = text;
    return true;
  }

  renderFilterField() {
    if (this.widget.isTextFilterFieldVisible()) {
      this._renderFilterField();
    } else {
      this._removeFilterField();
    }
  }

  _renderFilterField() {
    this._ensure$Container();
    this._filterField = scout.create('StringField', {
      parent: this.widget,
      label: this.widget.session.text('ui.Filter'),
      labelVisible: false,
      inheritAccessibility: false,
      cssClass: 'filter-field empty',
      fieldStyle: FormField.FieldStyle.CLASSIC,
      statusVisible: false,
      clearable: ValueField.Clearable.ALWAYS,
      updateDisplayTextOnModify: true,
      preventInitialFocus: true
    });
    this._filterField.render(this.$container);

    this._filterField.$field.attr('tabIndex', -1);

    if (!this.widget.rendered) {
      this.widget.session.layoutValidator.schedulePostValidateFunction(this._updateFilterFieldBackgroundColor.bind(this));
    } else {
      this._updateFilterFieldBackgroundColor();
    }

    this._textFilter = this._createTextFilter();
    this._textFilter.synthetic = true;
    this.addFilter(this._textFilter);

    this._filterField.on('propertyChange:displayText', this._filterFieldDisplayTextChangedHandler);
    this.widget.$container.on('focusin', this._focusInHandler);
    this.widget.$container.on('focusout', this._focusOutHandler);

    this.widget.$container.data('filter-field', this._filterField.$field);
    this._focusFilterFieldKeyStroke = new FocusFilterFieldKeyStroke(this.widget);
    this.widget.keyStrokeContext.registerKeyStroke(this._focusFilterFieldKeyStroke);

    this._cancelFilterFieldKeyStroke = new KeyStroke();
    this._cancelFilterFieldKeyStroke.field = this._filterField;
    this._cancelFilterFieldKeyStroke.which = [keys.ESC];
    this._cancelFilterFieldKeyStroke.stopPropagation = true;
    this._cancelFilterFieldKeyStroke.renderingHints.hAlign = HAlign.RIGHT;
    this._cancelFilterFieldKeyStroke.handle = this._cancelFilterField.bind(this);

    this._filterField.keyStrokeContext.registerKeyStroke(this._cancelFilterFieldKeyStroke);

    this._exitFilterFieldKeyStroke = new KeyStroke();
    this._exitFilterFieldKeyStroke.field = this._filterField;
    this._exitFilterFieldKeyStroke.which = [keys.ENTER];
    this._exitFilterFieldKeyStroke.stopPropagation = true;
    this._exitFilterFieldKeyStroke.renderingHints.hAlign = HAlign.RIGHT;
    this._exitFilterFieldKeyStroke.handle = this._exitFilterField.bind(this);

    this._filterField.keyStrokeContext.registerKeyStroke(this._exitFilterFieldKeyStroke);
  }

  _updateFilterFieldBackgroundColor() {
    if (!this._filterField || !this._filterField.rendered) {
      return;
    }
    let color = styles.getFirstOpaqueBackgroundColor(this._filterField.$container),
      colorRgba = $.extend(true, {red: 0, green: 0, blue: 0, alpha: 1}, styles.rgb(color)),
      transparent50Color = 'rgba(' + colorRgba.red + ', ' + colorRgba.green + ', ' + colorRgba.blue + ', ' + 0.5 + ')',
      transparent80Color = 'rgba(' + colorRgba.red + ', ' + colorRgba.green + ', ' + colorRgba.blue + ', ' + 0.8 + ')';
    this._filterField.$container.css('--filter-field-background-color', color);
    this._filterField.$container.css('--filter-field-transparent-50-background-color', transparent50Color);
    this._filterField.$container.css('--filter-field-transparent-80-background-color', transparent80Color);
  }

  _onFilterFieldDisplayTextChanged(event) {
    if (this._filterField && this._filterField.rendered) {
      this._filterField.$container.toggleClass('empty', !event.newValue);
    }
    if (!this._updateTextFilterText(this._textFilter, event.newValue)) {
      return;
    }
    this.filter();
  }

  _onFocusIn(event) {
    this._updateFocusInsideWidget(event.target);
  }

  _onFocusOut(event) {
    this._updateFocusInsideWidget(event.relatedTarget);
  }

  _updateFocusInsideWidget(target) {
    let focusInsideWidget = this.widget.$container.isOrHas(target);
    if (this._filterField && this._filterField.rendered) {
      this._filterField.$container.toggleClass('focus-inside-widget', focusInsideWidget);
    }
    return focusInsideWidget;
  }

  _exitFilterField() {
    this.widget.focus();
  }

  _cancelFilterField() {
    this._resetFilterField();
    this._exitFilterField();
  }

  _resetFilterField() {
    if (this._filterField) {
      this._filterField.setValue(null);
    }
  }

  _removeFilterField() {
    if (!this._filterField) {
      return;
    }
    this.widget.$container.off('focusin', this._focusInHandler);
    this.widget.$container.off('focusout', this._focusOutHandler);

    this.widget.keyStrokeContext.unregisterKeyStroke(this._focusFilterFieldKeyStroke);
    this._focusFilterFieldKeyStroke = null;

    this._resetFilterField();
    this._filterField.destroy();
    this._filterField = null;

    this.removeFilter(this._textFilter);
    this._textFilter = null;
  }

  remove() {
    this._removeFilterField();
  }

  /**
   * @param {Filter|function|(Filter|function)[]} filter The filters to add.
   * @param {boolean} applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   * @return {Filter[]} Returns the added filters.
   */
  addFilter(filter, applyFilter = true) {
    let filtersToAdd = arrays.ensure(filter);
    let filters = this._getFilters().slice(),
      oldFilters = filters.slice();
    filtersToAdd.forEach(f => {
      if (this._hasFilter(filters, f)) {
        return;
      }
      filters.push(f);
    });
    if (filters.length === this._getFilters().length) {
      return [];
    }
    this._setFilters(filters, applyFilter);

    let newFilters = this._getFilters().slice();
    arrays.removeAll(newFilters, oldFilters);

    return newFilters;
  }

  /**
   * @param {Filter|function|(Filter|function)[]} filter The filters to remove.
   * @param {boolean} applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   * @return {Filter[]} Returns the removed filters.
   */
  removeFilter(filter, applyFilter = true) {
    let filtersToRemove = arrays.ensure(filter);
    let filters = this._getFilters().slice(),
      oldFilters = filters.slice();

    let changed = false;
    filtersToRemove.forEach(f => {
      f = this._findFilter(filters, f);
      changed = arrays.remove(filters, f) || changed;
    });

    if (!changed) {
      return [];
    }
    this._setFilters(filters, applyFilter);

    let newFilters = this._getFilters().slice();
    arrays.removeAll(oldFilters, newFilters);

    return oldFilters;
  }

  /**
   * @param {Filter|function|(Filter|function)[]} filter The new filters.
   * @param {boolean} applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   * @return {SetFiltersResult}
   */
  setFilters(filters, applyFilter = true) {
    filters = arrays.ensure(filters);
    this._addSyntheticFilters(filters);

    if (this._getFilters().length === filters.length && filters.every(filter => this._hasFilter(this._getFilters(), filter))) {
      return {
        filtersAdded: [],
        filtersRemoved: []
      };
    }

    let oldFilters = this._getFilters().slice();

    this._setFilters(filters, applyFilter);

    let filtersAdded = this._getFilters().filter(filter => !this._findFilter(oldFilters, filter)),
      filtersRemoved = oldFilters.filter(filter => !this._findFilter(this._getFilters(), filter));

    return {
      filtersAdded: filtersAdded,
      filtersRemoved: filtersRemoved
    };
  }

  _setFilters(filters, applyFilter = true) {
    this.widget.setProperty('filters', filters.map(filter => {
      if (objects.isFunction(filter)) {
        return this._createFilterByFunction(filter);
      }
      return filter;
    }));
    if (applyFilter) {
      this.filter();
    }
  }

  _addSyntheticFilters(filters) {
    this._getFilters().filter(filter => filter.synthetic)
      .forEach(filter => this._addSyntheticFilter(filters, filter));

    this._addSyntheticFilter(filters, this._textFilter);
  }

  _addSyntheticFilter(filters, syntheticFilter) {
    if (!syntheticFilter || this._hasFilter(filters, syntheticFilter)) {
      return;
    }
    filters.push(syntheticFilter);
  }

  _getFilters() {
    return this.widget.filters;
  }

  _findFilter(filters, filter) {
    if (objects.isFunction(filter)) {
      return this._getFilterCreatedByFunction(filters, filter);
    }
    return arrays.find(filters, f => objects.equals(f, filter));
  }

  _getFilterCreatedByFunction(filters, filterFunc) {
    return arrays.find(filters, filter => filter.createdByFunction && filter.accept === filterFunc);
  }

  _hasFilter(filters, filter) {
    return !!this._findFilter(filters, filter);
  }

  _createFilterByFunction(filterFunc) {
    return {
      createdByFunction: true,
      accept: filterFunc
    };
  }

  filter() {
    let result = this._filterElements(),
      changed = result && (arrays.ensure(result.newlyHidden).length || arrays.ensure(result.newlyShown).length);

    if (changed) {
      this.widget.filteredElementsDirty = true;

      let opts = {};
      if (this._filterField) {
        opts.textFilterText = this._filterField.displayText;
      }
      this.widget.updateFilteredElements(result, opts);
    }

    return result;
  }

  _filter() {
    return this.applyFilters(this._getElementsForFiltering(), true);
  }

  applyFilters(elements, fullReset) {
    if (this._getFilters().length === 0 && !scout.nvl(fullReset, false)) {
      return;
    }
    let newlyShown = [];
    let newlyHidden = [];
    elements.forEach(element => {
      if (this.applyFiltersForElement(element)) {
        if (element.filterAccepted) {
          newlyShown.push(element);
        } else {
          newlyHidden.push(element);
        }
      }
    });

    return {
      newlyHidden: newlyHidden,
      newlyShown: newlyShown
    };
  }

  applyFiltersForElement(element) {
    if (this.elementAcceptedByFilters(element)) {
      if (!element.filterAccepted) {
        element.setFilterAccepted(true);
        return true;
      }
    } else if (element.filterAccepted) {
      element.setFilterAccepted(false);
      return true;
    }
    return false;
  }

  elementAcceptedByFilters(element) {
    return this._getFilters().every(filter => filter.accept(element));
  }
}
