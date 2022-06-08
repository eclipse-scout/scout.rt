export default interface Filter {
    /**
     * A function that returns true or false, whether the filter accepts the element or not.
     */
    accept: Function
}

export interface FilterResult {
    /**
     * An array of the newly hidden elements.
     */
    newlyHidden: object[],

    /**
     * An array of the newly shown elements.
     */
    newlyShown: object[]
}

export interface SetFiltersResult {
    /**
     * An array of the filters added.
     */
    filtersAdded: Filter[],

    /**
     * An array of the filters removed.
     */
    filtersRemoved: Filter[]
}