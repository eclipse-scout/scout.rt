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

  if (!this.popup.hasFilterFields && !this.popup.hasFilterTable) {
    return;
  }

  var
    actionColumnSize, filterColumnSize,
    $filterColumn = this.popup.$columnFilters,
    filterColumnInsets = scout.graphics.insets($filterColumn),
    filterColumnMargins = scout.graphics.margins($filterColumn),
    filterFieldGroupSize = new scout.Dimension();

  if (this.popup.$body.hasClass('compact')) {
    // height is auto -> read pref size
    filterColumnSize = scout.HtmlComponent.get($filterColumn).prefSize();
  } else {
    // make filter column as height as body (since body is scrollable pref size is calculated which takes TABLE_MAX_HEIGHT into account)
    filterColumnSize = this.preferredLayoutSize($container).subtract(scout.graphics.insets($container));
    filterColumnSize = filterColumnSize.subtract(filterColumnMargins);
  }
  // width is always set with css
  filterColumnSize.width = $filterColumn.cssWidth();

  // Set explicit height, necessary if there is no filter table
  $filterColumn.cssHeight(filterColumnSize.height);

  // TODO [7.0] cgu this code could be written a lot easier -> replace following code (filter fields, filter table) with scout.HtmlComponent.get($filterColumn).setSize(filterColumnSize);
  // Same for pref size. To implement max height of table, the RowLayout could read css max-height
  // Filter fields
  if (this.popup.hasFilterFields) {
    var
      $filterFieldsGroup = this.popup.$filterFieldsGroup,
      filterFieldHtmlComp = scout.HtmlComponent.get($filterFieldsGroup.find('.form-field'));

    // Layout filter field(s) and get size
    filterFieldHtmlComp.setSize(new scout.Dimension(filterColumnSize.width - filterColumnInsets.horizontal(), this._filterFieldsGroupBoxHeight()));
    filterFieldGroupSize = scout.graphics.size($filterFieldsGroup, true);
  }

  // Filter table
  if (this.popup.hasFilterTable) {
    var
      filterTableContainerHeight,
      $filterTableGroup = this.popup.$filterTableGroup,
      filterTableContainerInsets = scout.graphics.insets($filterTableGroup),
      filterTableHtmlComp = this.popup.filterTable.htmlComp;

    filterTableContainerHeight = filterColumnSize.height;
    // subtract height of filter-fields container
    filterTableContainerHeight -= filterFieldGroupSize.height;
    // subtract group-title height
    filterTableContainerHeight -= this._groupTitleHeight($filterTableGroup);
    // subtract insets of table container
    filterTableContainerHeight -= filterTableContainerInsets.vertical();
    // limit height of table
    filterTableContainerHeight = Math.min(filterTableContainerHeight, scout.TableHeaderMenuLayout.TABLE_MAX_HEIGHT);

    // Layout filter table
    filterTableHtmlComp.setSize(new scout.Dimension(
      filterColumnSize.width - filterColumnInsets.horizontal(),
      filterTableContainerHeight));
  }

  // fix width of actions column, so it doesn't become wider when user
  // hovers over a button and thus the text of the group changes.
  this._setMaxWidth();
  actionColumnSize = scout.graphics.size(this.popup.$columnActions);
  this._setMaxWidth(actionColumnSize.width);
};

scout.TableHeaderMenuLayout.prototype._adjustSizeWithAnchor = function(prefSize) {
  var maxWidth,
    htmlComp = this.popup.htmlComp,
    windowPaddingX = this.popup.windowPaddingX,
    popupMargins = htmlComp.margins(),
    popupBounds = htmlComp.offsetBounds(),
    $window = this.popup.$container.window(),
    windowWidth = $window.width();

  maxWidth = (windowWidth - popupMargins.horizontal() - popupBounds.x - windowPaddingX);
  var compact = popupBounds.width > maxWidth;
  if (compact) {
    this.popup.$body.addClass('compact', compact);
    prefSize = this.preferredLayoutSize(this.popup.$container);
  }

  return scout.TableHeaderMenuLayout.parent.prototype._adjustSizeWithAnchor.call(this, prefSize);
};

// group title (size used for table + field container)
scout.TableHeaderMenuLayout.prototype._groupTitleHeight = function($group) {
  return scout.graphics.size($group.find('.table-header-menu-group-text'), true).height;
};

scout.TableHeaderMenuLayout.prototype._filterFieldsGroupBoxHeight = function() {
  return this.popup.filterFieldsGroupBox.htmlComp.prefSize().height;
};

/**
 * The preferred layout size of this widget is
 * + size of table (but height is limited to TABLE_MAX_HEIGHT, if table becomes too large)
 * + size of filter-fields
 * + paddings of surrounding containers
 */
scout.TableHeaderMenuLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize, filterColumnMargins,
    rightColumnHeight = 0,
    leftColumnHeight = 0,
    containerInsets = scout.graphics.insets($container),
    oldMaxWidth = this._getMaxWidth();

  this._setMaxWidth(); // temp. remove max-width so we can determine pref. size
  leftColumnHeight = scout.graphics.size(this.popup.$columnActions, true).height;

  // Filter table
  if (this.popup.hasFilterTable) {
    var
      $filterTableGroup = this.popup.$filterTableGroup,
      filterTableHeight = this.popup.filterTable.htmlComp.size(true).height,
      filterTableContainerInsets = scout.graphics.insets($filterTableGroup),
      filterTableContainerHeight;

    // limit height of table
    filterTableHeight = Math.min(filterTableHeight, scout.TableHeaderMenuLayout.TABLE_MAX_HEIGHT);
    // size of container with table
    filterTableContainerHeight = filterTableHeight;
    // add group-title height
    filterTableContainerHeight += this._groupTitleHeight($filterTableGroup);
    // add insets of container
    filterTableContainerHeight += filterTableContainerInsets.vertical();

    rightColumnHeight += filterTableContainerHeight;
  }

  // Filter fields
  if (this.popup.hasFilterFields) {
    var
      $filterFieldsGroup = this.popup.$filterFieldsGroup,
      filterFieldContainerInsets = scout.graphics.insets($filterFieldsGroup),
      filterFieldContainerHeight;

    // size of group-box with 1 or 2 filter fields
    filterFieldContainerHeight = this._filterFieldsGroupBoxHeight();
    // add group-title height
    filterFieldContainerHeight += this._groupTitleHeight($filterFieldsGroup);
    // add insets of container
    filterFieldContainerHeight += filterFieldContainerInsets.vertical();

    rightColumnHeight += filterFieldContainerHeight;
  }

  if (this.popup.hasFilterFields || this.popup.hasFilterTable) {
    filterColumnMargins = scout.graphics.margins(this.popup.$columnFilters);
    rightColumnHeight += filterColumnMargins.vertical();
  }

  // Use height of left or right column as preferred size (and add insets of container)
  prefSize = scout.graphics.prefSize($container);
  if (!this.popup.$body.hasClass('compact')) {
    prefSize.height = Math.max(leftColumnHeight, rightColumnHeight) + containerInsets.vertical();
  } else {
    prefSize.height = leftColumnHeight + rightColumnHeight + containerInsets.vertical();
  }

  // restore max-width
  this._setMaxWidth(oldMaxWidth);

  return prefSize;
};

scout.TableHeaderMenuLayout.prototype._getMaxWidth = function() {
  return this.popup.$columnActions.css('max-width');
};

scout.TableHeaderMenuLayout.prototype._setMaxWidth = function(maxWidth) {
  this.popup.$columnActions.css('max-width', maxWidth || '');
};
