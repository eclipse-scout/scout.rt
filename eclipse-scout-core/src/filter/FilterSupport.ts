/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, EventHandler, Filter, Filterable, FilterElement, FilterResult, FilterSupportOptions, FocusFilterFieldKeyStroke, FormField, HAlign, keys, KeyStroke, objects, Predicate, PropertyChangeEvent, scout, SetFiltersResult, StringField,
  strings, styles, TextFilter, UpdateFilteredElementsOptions, ValueField, Widget, WidgetSupport
} from '../index';

export type FilterOrFunction<TElem extends FilterElement> = Filter<TElem> | Predicate<TElem>;

export class FilterSupport<TElem extends FilterElement> extends WidgetSupport {
  declare widget: Widget & Filterable<TElem>;
  protected _cancelFilterFieldKeyStroke: KeyStroke;
  protected _createTextFilter: () => TextFilter<TElem>;
  protected _exitFilterFieldKeyStroke: KeyStroke;
  protected _filterElements: () => FilterResult<TElem>;
  protected _filterField: StringField;
  protected _filterFieldDisplayTextChangedHandler: EventHandler<PropertyChangeEvent<string>>;
  protected _focusFilterFieldKeyStroke: KeyStroke;
  protected _focusInHandler: (event: JQuery.FocusInEvent) => void;
  protected _focusOutHandler: (event: JQuery.FocusOutEvent) => void;
  protected _getElementText: (elem: TElem) => string;
  protected _getElementsForFiltering: () => TElem[];
  protected _textFilter: TextFilter<TElem>;
  protected _updateTextFilterText: (filter: TextFilter<TElem>, text: string) => boolean;

  /**
   * @param options a mandatory options object
   */
  constructor(options: FilterSupportOptions<TElem>) {
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

  protected _createDefaultTextFilter(): TextFilter<TElem> {
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

  protected _updateDefaultTextFilterText(filter: TextFilter<TElem>, text: string): boolean {
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

  protected _renderFilterField() {
    this._ensure$Container();
    this._filterField = scout.create(StringField, {
      parent: this.widget,
      label: this.widget.session.text('ui.Filter'),
      labelVisible: false,
      inheritAccessibility: false,
      cssClass: 'filter-field empty',
      fieldStyle: FormField.FieldStyle.CLASSIC,
      statusVisible: false,
      clearable: ValueField.Clearable.ALWAYS,
      updateDisplayTextOnModify: true,
      preventInitialFocus: true,
      checkSaveNeeded: false
    });
    this._filterField.render(this.$container);

    this._filterField.$field.attr('tabindex', -1);

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

  protected _updateFilterFieldBackgroundColor() {
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

  protected _onFilterFieldDisplayTextChanged(event: PropertyChangeEvent<string>) {
    if (this._filterField && this._filterField.rendered) {
      this._filterField.$container.toggleClass('empty', !event.newValue);
    }
    if (!this._updateTextFilterText(this._textFilter, event.newValue)) {
      return;
    }
    this.filter();
  }

  protected _onFocusIn(event: JQuery.FocusInEvent) {
    this._updateFocusInsideWidget(event.target);
  }

  protected _onFocusOut(event: JQuery.FocusOutEvent) {
    this._updateFocusInsideWidget(event.relatedTarget as Element);
  }

  protected _updateFocusInsideWidget(target: Element): boolean {
    let focusInsideWidget = this.widget.$container.isOrHas(target);
    if (this._filterField && this._filterField.rendered) {
      this._filterField.$container.toggleClass('focus-inside-widget', focusInsideWidget);
    }
    return focusInsideWidget;
  }

  protected _exitFilterField() {
    this.widget.focus();
  }

  protected _cancelFilterField() {
    this._resetFilterField();
    this._exitFilterField();
  }

  protected _resetFilterField() {
    if (this._filterField) {
      this._filterField.setValue(null);
    }
  }

  protected _removeFilterField() {
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
   * @param filter The filters to add.
   * @param applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   * @returns the added filters.
   */
  addFilter(filter: FilterOrFunction<TElem> | FilterOrFunction<TElem>[], applyFilter = true): Filter<TElem>[] {
    let filtersToAdd = arrays.ensure(filter);
    let filters: FilterOrFunction<TElem>[] = this._getFilters().slice();
    let oldFilters = filters.slice();
    filtersToAdd.forEach(filter => {
      if (this._hasFilter(filters, filter)) {
        return;
      }
      filters.push(filter);
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
   * @param filter The filters to remove.
   * @param applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   * @returns the removed filters.
   */
  removeFilter(filter: FilterOrFunction<TElem> | FilterOrFunction<TElem>[], applyFilter = true): Filter<TElem>[] {
    let filtersToRemove = arrays.ensure(filter);
    let filters = this._getFilters().slice(),
      oldFilters = filters.slice();

    let changed = false;
    filtersToRemove.forEach(filter => {
      filter = this._findFilter(filters, filter);
      changed = arrays.remove(filters, filter) || changed;
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
   * @param filter The new filters.
   * @param applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  setFilters(filters: FilterOrFunction<TElem> | FilterOrFunction<TElem>[], applyFilter = true): SetFiltersResult<TElem> {
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

  protected _setFilters(filters: FilterOrFunction<TElem>[], applyFilter = true) {
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

  protected _addSyntheticFilters(filters: FilterOrFunction<TElem>[]) {
    this._getFilters().filter(filter => filter.synthetic)
      .forEach(filter => this._addSyntheticFilter(filters, filter));

    this._addSyntheticFilter(filters, this._textFilter);
  }

  protected _addSyntheticFilter(filters: FilterOrFunction<TElem>[], syntheticFilter: Filter<TElem>) {
    if (!syntheticFilter || this._hasFilter(filters, syntheticFilter)) {
      return;
    }
    filters.push(syntheticFilter);
  }

  protected _getFilters(): Filter<TElem>[] {
    return this.widget.filters;
  }

  protected _findFilter(filters: FilterOrFunction<TElem>[], filter: FilterOrFunction<TElem>): Filter<TElem> {
    if (objects.isFunction(filter)) {
      return this._getFilterCreatedByFunction(filters, filter);
    }
    return arrays.find(filters, f => objects.equals(f, filter)) as Filter<TElem>;
  }

  protected _getFilterCreatedByFunction(filters: FilterOrFunction<TElem>[], filterFunc: Predicate<TElem>): Filter<TElem> {
    return arrays.find(filters, filter => typeof filter !== 'function' && filter.createdByFunction && filter.accept === filterFunc) as Filter<TElem>;
  }

  protected _hasFilter(filters: FilterOrFunction<TElem>[], filter: FilterOrFunction<TElem>): boolean {
    return !!this._findFilter(filters, filter);
  }

  protected _createFilterByFunction(filterFunc: Predicate<TElem>): Filter<TElem> {
    return {
      createdByFunction: true,
      accept: filterFunc
    };
  }

  filter(): FilterResult<TElem> {
    let result = this._filterElements();
    let changed = result && (arrays.ensure(result.newlyHidden).length || arrays.ensure(result.newlyShown).length);

    if (changed) {
      this.widget.filteredElementsDirty = true;

      let opts = {} as UpdateFilteredElementsOptions;
      if (this._filterField) {
        opts.textFilterText = this._filterField.displayText;
      }
      this.widget.updateFilteredElements(result, opts);
    }

    return result;
  }

  protected _filter(): FilterResult<TElem> {
    return this.applyFilters(this._getElementsForFiltering(), true);
  }

  applyFilters(elements: TElem[], fullReset?: boolean): FilterResult<TElem> {
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

  applyFiltersForElement(element: TElem): boolean {
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

  elementAcceptedByFilters(element: TElem): boolean {
    return this._getFilters().every(filter => filter.accept(element));
  }
}
