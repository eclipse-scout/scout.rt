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

  var
    $filterColumn = this.popup.$columnFilters,
    filterColumnSize = scout.graphics.getSize($filterColumn),
    filterColumnInsets = scout.graphics.getInsets($filterColumn),
    $filterFieldsGroup = this.popup.$filterFieldsGroup,
    filterFieldGroupSize,
    filterFieldHtmlComp = scout.HtmlComponent.get($filterFieldsGroup.find('.form-field')),
    $filterTableGroup = this.popup.$filterTableGroup,
    filterTableContainerInsets = scout.graphics.getInsets($filterTableGroup),
    filterTableContainerHeight,
    filterTableHtmlComp = this.popup.filterTable.htmlComp;

  // Layout filter field(s) and get size
  filterFieldHtmlComp.setSize(new scout.Dimension(filterColumnSize.width - filterColumnInsets.horizontal(), this._filterFieldsGroupBoxHeight()));
  filterFieldGroupSize = scout.graphics.getSize($filterFieldsGroup, true);

  filterTableContainerHeight = filterColumnSize.height;
  // subtract height of filter-fields container
  filterTableContainerHeight -= filterFieldGroupSize.height;
  // subtract group-title height
  filterTableContainerHeight -= this._groupTitleHeight($filterTableGroup);
  // subtract insets of table container
  filterTableContainerHeight -= filterTableContainerInsets.vertical();

  // Layout filter table
  filterTableHtmlComp.pixelBasedSizing = true;
  filterTableHtmlComp.setSize(new scout.Dimension(
      filterColumnSize.width - filterColumnInsets.horizontal(),
      filterTableContainerHeight));
};

//group title (size used for table + field container)
scout.TableHeaderMenuLayout.prototype._groupTitleHeight = function($group) {
  return scout.graphics.getSize($group.find('.table-header-menu-group-text'), true).height;
};

scout.TableHeaderMenuLayout.prototype._filterFieldsGroupBoxHeight = function() {
  return this.popup.filterFieldsGroupBox.htmlComp.getPreferredSize().height;
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
    $filterTableGroup = this.popup.$filterTableGroup,
    filterTableHeight = this.popup.filterTable.htmlComp.getSize(true).height,
    filterTableContainerInsets = scout.graphics.getInsets($filterTableGroup),
    filterTableContainerHeight,
    // filter field(s) container
    filterFieldContainerInsets = scout.graphics.getInsets(this.popup.$filterFieldsGroup),
    filterFieldContainerHeight,
    groupTitleHeight = this._groupTitleHeight($filterTableGroup),
    leftColumnHeight = scout.graphics.getSize(this.popup.$columnActions, true).height,
    rightColumnHeight,
    prefSize;

  // limit height of table
  filterTableHeight = Math.min(filterTableHeight, scout.TableHeaderMenuLayout.TABLE_MAX_HEIGHT);
  // size of container with table
  filterTableContainerHeight = filterTableHeight;
  // add group-title height
  filterTableContainerHeight += groupTitleHeight;
  // add insets of container
  filterTableContainerHeight += filterTableContainerInsets.vertical();


  // size of group-box with 1 or 2 filter fields
  filterFieldContainerHeight = this._filterFieldsGroupBoxHeight();
  // add group-title height
  filterFieldContainerHeight += groupTitleHeight;
  // add insets of container
  filterFieldContainerHeight += filterFieldContainerInsets.vertical();

  rightColumnHeight = filterTableContainerHeight + filterFieldContainerHeight;

  // Use height of left or right column as preferred size (and add insets of container)
  prefSize = scout.graphics.prefSize($container);
  prefSize.height = Math.max(leftColumnHeight, rightColumnHeight) + containerInsets.vertical();
  return prefSize;
};
