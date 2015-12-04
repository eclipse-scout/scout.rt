/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableHeaderMenuLayout = function(popup) {
  scout.TableHeaderMenuLayout.parent.call(this, popup);
  this.popup = popup;
};
scout.inherits(scout.TableHeaderMenuLayout, scout.PopupLayout);

/**
 * Don't use scout.HtmlEnvironment.formRowHeight here intentionally (field looks to large)
 * Now it has the same height as the buttons in the left column.
 */
scout.TableHeaderMenuLayout.TEXT_FIELD_HEIGHT = 29;

scout.TableHeaderMenuLayout.prototype.layout = function($container) {
  scout.TableHeaderMenuLayout.parent.prototype.layout.call(this, $container);

  // FIXME AWE: (filter) cleanup naming / properties for filtering DIVs
  var
    $columnFilters = this.popup.$columnFilters,
    columnFiltersSize = scout.graphics.getSize($columnFilters),
    $groupedFilterValuesContainer = this.popup.$filtering,
    groupedFilterValuesContainerHeight,
    groupedFilterValuesContainerScrollHeight = $columnFilters.get(0).scrollHeight,
    groupedFilterValuesInsets = scout.graphics.getInsets($groupedFilterValuesContainer),
    groupedFilterValuesTitleSize = scout.graphics.getSize($groupedFilterValuesContainer.find('.table-header-menu-group-text')),
    $filterFieldContainer = this.popup.$columnFilters.find('.table-header-menu-filter-field'),
    filterFieldContainerInsets = scout.graphics.getInsets($filterFieldContainer),
    filterFieldContainerSize,
    filterFieldHtmlComp = scout.HtmlComponent.get($filterFieldContainer.find('.form-field'));

  // Layout filter field(s)
  filterFieldHtmlComp.setSize(new scout.Dimension(columnFiltersSize.width - filterFieldContainerInsets.horizontal(), scout.TableHeaderMenuLayout.TEXT_FIELD_HEIGHT));
  filterFieldContainerSize = scout.graphics.getSize($filterFieldContainer, true);

  // Layout grouped filter values list
  columnFiltersSize = columnFiltersSize
    .subtract(scout.graphics.getInsets($columnFilters));
  groupedFilterValuesContainerHeight = columnFiltersSize.height - filterFieldContainerSize.height;

  // If there are only some filter items make container smaller, otherwise use given height
  groupedFilterValuesContainerHeight = Math.min(groupedFilterValuesContainerHeight, groupedFilterValuesContainerScrollHeight);
  $groupedFilterValuesContainer.cssHeight(groupedFilterValuesContainerHeight);

  // Layout scrollable list
  this.popup.filterTable.htmlComp.pixelBasedSizing = true;
  this.popup.filterTable.htmlComp.setSize(new scout.Dimension(
      columnFiltersSize.width - 20, // FIXME AWE: (filter) read padding from group
      groupedFilterValuesContainerHeight - groupedFilterValuesInsets.vertical() - groupedFilterValuesTitleSize.height));
};
