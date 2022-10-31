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
import {Filterable, FilterElement, FilterResult, TextFilter} from './Filter';
import {Widget, WidgetSupportOptions} from '../index';

export default interface FilterSupportOptions<TElem extends FilterElement> extends WidgetSupportOptions {
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
