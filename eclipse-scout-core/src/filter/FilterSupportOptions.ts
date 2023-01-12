/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Filterable, FilterElement, FilterResult, TextFilter, Widget, WidgetSupportOptions} from '../index';

export interface FilterSupportOptions<TElem extends FilterElement> extends WidgetSupportOptions {
  widget: Widget & Filterable<TElem>;

  /**
   * Filter all elements.
   */
  filterElements?(): FilterResult<TElem>;

  /**
   * Get all elements to which the filters should be applied. Mandatory if no {@link filterElements} is specified.
   */
  getElementsForFiltering?(): TElem[];

  /**
   * Get text of an element.
   */
  getElementText?(elem: TElem): string;

  /**
   * Create a text filter. Optional
   */
  createTextFilter?(): TextFilter<TElem>;

  /**
   * Update the text on the filter, this is mandatory if {@link createTextFilter} is set.
   */
  updateTextFilterText?(filter: TextFilter<TElem>, text: string): boolean;
}
