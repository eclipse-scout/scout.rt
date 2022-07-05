import {WidgetSupportOptions} from './WidgetSupport';

export default interface FilterSupportOptions extends WidgetSupportOptions {
  /**
   * Filter all elements.
   */
  filterElements: Function,
  /**
   * Get all elements to which the filters should be applied.
   */
  getElementsForFiltering: Function,
  /**
   * Get text of an element.
   */
  getElementText: Function,
  /**
   * Create a text filter.
   */
  createTextFilter: Function,
  /**
   * Update the text on the filter, this is mandatory if createTextFilter is set.
   */
  updateTextFilterText: Function
}

