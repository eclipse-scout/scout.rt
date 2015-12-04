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

scout.TableHeaderMenuLayout.TABLE_MAX_HEIGHT = 330;

/**
 * When this layout method is called we've calculated the pref. size before. Layout does this:
 * - layout the filter-fields (their size is fixed)
 * - use the remaining height to layout the filter table
 */
scout.TableHeaderMenuLayout.prototype.layout = function($container) {
  scout.TableHeaderMenuLayout.parent.prototype.layout.call(this, $container);

  // FIXME AWE: (filter) cleanup naming / properties for filtering DIVs
  var
    $filterColumn = this.popup.$columnFilters,
    filterColumnSize = scout.graphics.getSize($filterColumn),
    filterColumnInsets = scout.graphics.getInsets($filterColumn),
    $filterFieldContainer = $filterColumn.find('.table-header-menu-filter-field'),
    filterFieldContainerInsets = scout.graphics.getInsets($filterFieldContainer),
    filterFieldContainerSize,
    filterFieldHtmlComp = scout.HtmlComponent.get($filterFieldContainer.find('.form-field')),
    $filterTableContainer = this.popup.$filtering,
    filterTableContainerInsets = scout.graphics.getInsets($filterTableContainer),
    filterTableContainerHeight,
    groupTitleSize = scout.graphics.getSize($filterTableContainer.find('.table-header-menu-group-text'), true);

  // Layout filter field(s) and get size
  filterFieldHtmlComp.setSize(new scout.Dimension(filterColumnSize.width - filterColumnInsets.horizontal(), scout.TableHeaderMenuLayout.TEXT_FIELD_HEIGHT));
  filterFieldContainerSize = scout.graphics.getSize($filterFieldContainer, true);

  filterTableContainerHeight = filterColumnSize.height;
  // subtract height of filter-fields container
  filterTableContainerHeight -= filterFieldContainerSize.height;
  // subtract group-title height
  filterTableContainerHeight -= groupTitleSize.height;
  // subtract insets of table container
  filterTableContainerHeight -= filterTableContainerInsets.vertical();

  // Layout filter table
  this.popup.filterTable.htmlComp.pixelBasedSizing = true;
  this.popup.filterTable.htmlComp.setSize(new scout.Dimension(
      filterColumnSize.width - 20, // FIXME AWE: (filter) read padding from group
      filterTableContainerHeight));
};

/**
 * The preferred layout size of this widget is
 * + size of table (but height is limited to TABLE_MAX_HEIGHT, if table becomes too large)
 * + size of filter-fields
 * + paddings of surrounding containers
 */
scout.TableHeaderMenuLayout.prototype.preferredLayoutSize = function($container) {
  var
    containerInsets = scout.graphics.getInsets($container),
    // filter table container
    $filterTableContainer = this.popup.$filtering,
    filterTableSize = scout.graphics.getSize($filterTableContainer.find('.table'), true),
    filterTableHeight = filterTableSize.height,
    filterTableContainerInsets = scout.graphics.getInsets($filterTableContainer),
    filterTableContainerHeight,
    // filter field(s) container
    $filterFieldContainer = this.popup.$columnFilters.find('.table-header-menu-filter-field'),
    filterFieldContainerInsets = scout.graphics.getInsets($filterFieldContainer),
    filterFieldContainerHeight,
    filterFieldHtmlComp = scout.HtmlComponent.get($filterFieldContainer.find('.form-field')),
    // group title (size used for table + field container)
    groupTitleSize = scout.graphics.getSize($filterTableContainer.find('.table-header-menu-group-text'), true);


  // limit height of table
  filterTableHeight = Math.min(filterTableHeight, scout.TableHeaderMenuLayout.TABLE_MAX_HEIGHT);
  // size of container with table
  filterTableContainerHeight = filterTableHeight;
  // add group-title height
  filterTableContainerHeight += groupTitleSize.height;
  // add insets of container
  filterTableContainerHeight += filterTableContainerInsets.vertical();


  // size of filter fields
  filterFieldContainerHeight = scout.TableHeaderMenuLayout.TEXT_FIELD_HEIGHT;
  // add group-title height
  filterFieldContainerHeight += groupTitleSize.height;
  // add insets of container
  filterFieldContainerHeight += filterFieldContainerInsets.vertical();


  var prefSize = scout.graphics.prefSize($container);
  prefSize.height = (filterTableContainerHeight + filterFieldContainerHeight + containerInsets.horizontal()); // FIXME AWE: (filter) add paddings of surrounding div (table-header-menu)
  return prefSize;
};
