/* eslint-disable max-classes-per-file */
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
import {FilterSupport, HtmlComponent, KeyStrokeContext, NullWidget, numbers, strings} from '../../src/index';
import $ from 'jquery';

describe('FilterSupport', () => {

  let session;

  class FilterSupportWidget extends NullWidget {
    constructor() {
      super();
      this.elements = [];

      this.filters = [];
      this.textFilterEnabled = false;
      this.filterSupport = this._createFilterSupport();

      this.filteredElementsDirty = false;
    }

    _render() {
      this.$container = this.$parent.appendDiv();
      this.htmlComp = HtmlComponent.install(this.$container, this.session);
    }

    _renderProperties() {
      super._renderProperties();
      this._renderTextFilterEnabled();
    }

    _remove() {
      this.filterSupport.remove();
      super._remove();
    }

    _init(model) {
      super._init(model);
      this.setElements(this.elements);
      this.setFilters(this.filters);
    }

    _createKeyStrokeContext() {
      return new KeyStrokeContext();
    }

    setElements(elements) {
      elements = elements.map(element => this._initElement(element));
      this.setProperty('elements', elements);
      this.filter();
    }

    _initElement(element) {
      if (!(element instanceof Element)) {
        let e = new Element();
        e.init(element);
        element = e;
      }
      return element;
    }

    filteredElements() {
      return this.elements.filter(element => element.filterAccepted);
    }

    addFilter(filter, applyFilter = true) {
      this.filterSupport.addFilter(filter, applyFilter);
    }

    removeFilter(filter, applyFilter = true) {
      this.filterSupport.removeFilter(filter, applyFilter);
    }

    setFilters(filters, applyFilter = true) {
      this.filterSupport.setFilters(filters, applyFilter);
    }

    filter() {
      this.filterSupport.filter();
    }

    updateFilteredElements(result, opts) {
      this.filteredElementsDirty = false;
    }

    /**
     * @returns {FilterSupport}
     */
    _createFilterSupport() {
      return new FilterSupport({
        widget: this,
        $container: () => this.$container,
        getElementsForFiltering: () => this.elements,
        getElementText: element => element.text
      });
    }

    setTextFilterEnabled(textFilterEnabled) {
      this.setProperty('textFilterEnabled', textFilterEnabled);
    }

    isTextFilterFieldVisible() {
      return this.textFilterEnabled;
    }

    _renderTextFilterEnabled() {
      this.filterSupport.renderFilterField();
    }
  }

  class Element {
    constructor() {
      this.id = null;
      this.text = null;
      this.someProperty = false;
      this.filterAccepted = true;
    }

    init(model) {
      $.extend(this, model);
    }

    setFilterAccepted(filterAccepted) {
      this.filterAccepted = filterAccepted;
    }
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createWidget(model) {
    let defaults = {
      parent: session.desktop,
      session: session
    };
    model = $.extend({}, defaults, model);
    let widget = new FilterSupportWidget();
    widget.init(model);
    return widget;
  }

  function createElements() {
    return [
      {id: 1, text: 'Hi! I am Marvin, the paranoid android'},
      {id: 2, text: 'Life? Don\'t talk to me about life.', someProperty: true},
      {id: 3, text: 'Here I am, brain the size of a planet, and they tell me to take you up to the bridge. Call that job satisfaction? \'Cos I don\'t.'},
      {id: 4, text: 'I think you ought to know I\'m feeling very depressed.'},
      {id: 5, text: 'Pardon me for breathing, which I never do anyway so I don\'t know why I bother to say it, Oh God, I\'m so depressed.', someProperty: true},
      {id: 6, text: 'There\'s only one life-form as intelligent as me within thirty parsecs of here and that\'s me', someProperty: true},
      {id: 7, text: 'I wish you\'d just tell me rather trying to engage my enthusiasm because I haven\'t got one.'}
    ];
  }

  describe('modify filters', () => {

    function testModifyFilter(somePropertyFilter, idIsPrimeFilter, depressedFilter) {
      let widget = createWidget({elements: createElements()});

      expect(widget.filters.length).toBe(0);
      expect(widget.filteredElements().length).toBe(7);

      widget.addFilter(somePropertyFilter);

      expect(widget.filters.length).toBe(1);
      expect(widget.filteredElements().length).toBe(3);

      widget.addFilter(idIsPrimeFilter, false);

      expect(widget.filters.length).toBe(2);
      expect(widget.filteredElements().length).toBe(3);

      widget.removeFilter(depressedFilter);

      expect(widget.filters.length).toBe(2);
      expect(widget.filteredElements().length).toBe(3);

      widget.filter();

      expect(widget.filters.length).toBe(2);
      expect(widget.filteredElements().length).toBe(2);

      widget.removeFilter(somePropertyFilter);

      expect(widget.filters.length).toBe(1);
      expect(widget.filteredElements().length).toBe(5);

      widget.addFilter(depressedFilter);

      expect(widget.filters.length).toBe(2);
      expect(widget.filteredElements().length).toBe(1);

      widget.setFilters([depressedFilter]);

      expect(widget.filters.length).toBe(1);
      expect(widget.filteredElements().length).toBe(2);

      widget.setFilters([], false);

      expect(widget.filters.length).toBe(0);
      expect(widget.filteredElements().length).toBe(2);

      widget.filter();

      expect(widget.filters.length).toBe(0);
      expect(widget.filteredElements().length).toBe(7);
    }

    it('filter objects', () => {
      let somePropertyFilter = {
          accept: element => element.someProperty
        },
        idIsPrimeFilter = {
          accept: element => {
            let n = element.id;
            if (!numbers.isInteger(n) || n < 1) {
              return false;
            }
            if (n % 2 === 0 && n > 2) {
              return false;
            }
            for (let i = 3; i <= Math.sqrt(n); i++) {
              if (n % i === 0) {
                return false;
              }
            }
            return true;
          }
        },
        depressedFilter = {
          accept: element => strings.contains(element.text, 'depressed')
        };

      testModifyFilter(somePropertyFilter, idIsPrimeFilter, depressedFilter);
    });

    it('filter functions', () => {
      let somePropertyFilterFunc = element => element.someProperty,
        idIsPrimeFilterFunc = element => {
          let n = element.id;
          if (!numbers.isInteger(n) || n < 1) {
            return false;
          }
          if (n % 2 === 0 && n > 2) {
            return false;
          }
          for (let i = 3; i <= Math.sqrt(n); i++) {
            if (n % i === 0) {
              return false;
            }
          }
          return true;
        },
        depressedFilterFunc = element => strings.contains(element.text, 'depressed');

      testModifyFilter(somePropertyFilterFunc, idIsPrimeFilterFunc, depressedFilterFunc);
    });
  });

  describe('text filter', () => {
    function testTextFilter(somePropertyFilter, idIsPrimeFilter) {
      let widget = createWidget({elements: createElements()});
      widget.render(session.$entryPoint);

      expect(widget.filters.length).toBe(0);
      expect(widget.filteredElements().length).toBe(7);
      expect(widget.filterSupport._filterField).toBeFalsy();

      widget.addFilter(somePropertyFilter);

      expect(widget.filters.length).toBe(1);
      expect(widget.filteredElements().length).toBe(3);
      expect(widget.filterSupport._filterField).toBeFalsy();

      widget.setTextFilterEnabled(false);

      expect(widget.filters.length).toBe(1);
      expect(widget.filteredElements().length).toBe(3);
      expect(widget.filterSupport._filterField).toBeFalsy();

      widget.setTextFilterEnabled(true);

      expect(widget.filters.length).toBe(2);
      expect(widget.filteredElements().length).toBe(3);
      expect(widget.filterSupport._filterField).toBeTruthy();

      widget.filterSupport._filterField.setValue('depressed');

      expect(widget.filters.length).toBe(2);
      expect(widget.filteredElements().length).toBe(1);
      expect(widget.filterSupport._filterField).toBeTruthy();

      widget.setFilters([idIsPrimeFilter], false);

      expect(widget.filters.length).toBe(2);
      expect(widget.filteredElements().length).toBe(1);
      expect(widget.filterSupport._filterField).toBeTruthy();

      widget.filterSupport._filterField.setValue(null);

      expect(widget.filters.length).toBe(2);
      expect(widget.filteredElements().length).toBe(5);
      expect(widget.filterSupport._filterField).toBeTruthy();

      widget.filterSupport._filterField.setValue('depressed');

      expect(widget.filters.length).toBe(2);
      expect(widget.filteredElements().length).toBe(1);
      expect(widget.filterSupport._filterField).toBeTruthy();

      widget.setFilters([]);

      expect(widget.filters.length).toBe(1);
      expect(widget.filteredElements().length).toBe(2);
      expect(widget.filterSupport._filterField).toBeTruthy();

      widget.setTextFilterEnabled(false);

      expect(widget.filters.length).toBe(0);
      expect(widget.filteredElements().length).toBe(7);
      expect(widget.filterSupport._filterField).toBeFalsy();

      widget.remove();
    }

    it('filter objects', () => {
      let somePropertyFilter = {
          accept: element => element.someProperty
        },
        idIsPrimeFilter = {
          accept: element => {
            let n = element.id;
            if (!numbers.isInteger(n) || n < 1) {
              return false;
            }
            if (n % 2 === 0 && n > 2) {
              return false;
            }
            for (let i = 3; i <= Math.sqrt(n); i++) {
              if (n % i === 0) {
                return false;
              }
            }
            return true;
          }
        };

      testTextFilter(somePropertyFilter, idIsPrimeFilter);
    });

    it('filter functions', () => {
      let somePropertyFilterFunc = element => element.someProperty,
        idIsPrimeFilterFunc = element => {
          let n = element.id;
          if (!numbers.isInteger(n) || n < 1) {
            return false;
          }
          if (n % 2 === 0 && n > 2) {
            return false;
          }
          for (let i = 3; i <= Math.sqrt(n); i++) {
            if (n % i === 0) {
              return false;
            }
          }
          return true;
        };

      testTextFilter(somePropertyFilterFunc, idIsPrimeFilterFunc);
    });
  });
});
