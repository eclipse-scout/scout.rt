enum TableMenuType {
  /**
   * Specifies menus which are visible independent of the selection of the table.<br>
   * The menu will be disabled if the table itself is disabled. If the menu has multiple types, the most restrictive
   * type wins (e.g. a menu with type EmptySpace and SingleSelection will be disabled if a disabled row is selected).
   */
  EmptySpace ='Table.EmptySpace',
  /**
   * Specifies menus which are visible if a single table row is selected.<br>
   * If the table row is disabled or the table itself is disabled, the menu will be disabled as well. If the menu has
   * multiple types, the most restrictive type wins (e.g. a menu with type EmptySpace and SingleSelection will be
   * disabled if a disabled row is selected).
   */
  SingleSelection ='Table.SingleSelection',
  /**
   * Specifies menus which are visible if multiple table rows are selected.<br>
   * If the selection contains disabled rows or the table itself is disabled, the menu will be disabled as well. If the
   * menu has multiple types, the most restrictive type wins (e.g. a menu with type EmptySpace and SingleSelection will
   * be disabled if a disabled row is selected).
   */
  MultiSelection = 'Table.MultiSelection',
  Header = 'Table.Header'
}
export default TableMenuType;
