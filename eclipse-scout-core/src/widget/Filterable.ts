import {Filter} from '../index';

export default interface Filterable {
  isTextFilterFieldVisible(): boolean,
  filters: Filter[],
  filteredElementsDirty: boolean,
  updateFilteredElements(result: any, opts: any)
}
