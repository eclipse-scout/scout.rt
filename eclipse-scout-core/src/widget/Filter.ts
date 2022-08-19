// FIXME TS mig move filter stuff to filter folder
import {Predicate} from '../types';

export default interface Filter<TElem extends FilterElement> {
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
  acceptedText: string;
}

export interface UpdateFilteredElementsOptions {
  textFilterText: string;
}

export interface Filterable<TElem extends FilterElement> {
  isTextFilterFieldVisible(): boolean;
  filters: Filter<TElem>[];
  filteredElementsDirty: boolean;
  updateFilteredElements(result: FilterResult<TElem>, options: UpdateFilteredElementsOptions);
}
