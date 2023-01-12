/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Predicate} from '../index';

/**
 * A filter is used to only accept specific elements and to hide all others.
 */
export interface Filter<TElem extends FilterElement> {
  /**
   * A function that returns true or false, whether the filter accepts the element or not.
   */
  accept: Predicate<TElem>;
  createdByFunction?: boolean;
  synthetic?: boolean;
}

export interface FilterElement {
  filterAccepted: boolean;
  setFilterAccepted(accepted: boolean);
}

export interface FilterResult<TElem extends FilterElement> {
  /**
   * An array of the newly hidden elements.
   */
  newlyHidden: TElem[];

  /**
   * An array of the newly shown elements.
   */
  newlyShown: TElem[];
}

export interface SetFiltersResult<TElem extends FilterElement> {
  /**
   * An array of the filters added.
   */
  filtersAdded: Filter<TElem>[];

  /**
   * An array of the filters removed.
   */
  filtersRemoved: Filter<TElem>[];
}

export interface TextFilter<TElem extends FilterElement> extends Filter<TElem> {
  acceptedText?: string;
}

export interface UpdateFilteredElementsOptions {
  textFilterText: string;
}

export interface Filterable<TElem extends FilterElement> {
  isTextFilterFieldVisible(): boolean;
  filters: Filter<TElem>[];
  filteredElementsDirty?: boolean;
  updateFilteredElements?(result: FilterResult<TElem>, options: UpdateFilteredElementsOptions);
}
