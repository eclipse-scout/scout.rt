import {Filterable, FilterElement, FilterResult, TextFilter} from './Filter';
import {Widget, WidgetSupportOptions} from '../index';

export default interface FilterSupportOptions<TElem extends FilterElement> extends WidgetSupportOptions {
  widget: Widget & Filterable<TElem>;

  /**
   * Filter all elements.
   */
  filterElements: () => FilterResult<TElem>;
  /**
   * Get all elements to which the filters should be applied.
   */
  getElementsForFiltering: () => TElem[];
  /**
   * Get text of an element.
   */
  getElementText: (elem: TElem) => string;
  /**
   * Create a text filter.
   */
  createTextFilter: () => TextFilter<TElem>;
  /**
   * Update the text on the filter, this is mandatory if createTextFilter is set.
   */
  updateTextFilterText: (filter: TextFilter<TElem>, text: string) => boolean;
}

