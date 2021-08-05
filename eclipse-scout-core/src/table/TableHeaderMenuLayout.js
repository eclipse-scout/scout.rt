/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Dimension, graphics, HtmlComponent, PopupLayout} from '../index';

export default class TableHeaderMenuLayout extends PopupLayout {

  constructor(popup) {
    super(popup);
    this.popup = popup;
    this.doubleCalcPrefSize = false;
  }

  static TABLE_MAX_HEIGHT = 330;

  /**
   * When this layout method is called we've calculated the pref. size before. Layout does this:
   * - layout the filter-fields (their size is fixed)
   * - use the remaining height to layout the filter table
   */
  layout($container) {
    super.layout($container);

    if (!this.popup.hasFilterFields && !this.popup.hasFilterTable) {
      return;
    }

    let
      actionColumnSize, filterColumnSize,
      $filterColumn = this.popup.$columnFilters,
      filterColumnInsets = graphics.insets($filterColumn),
      filterColumnMargins = graphics.margins($filterColumn),
      filterFieldGroupSize = new Dimension();

    if (this.popup.$body.hasClass('compact')) {
      // height is auto -> read pref size
      filterColumnSize = HtmlComponent.get($filterColumn).prefSize();
    } else {
      // make filter column as height as body (since body is scrollable pref size is calculated which takes TABLE_MAX_HEIGHT into account)
      filterColumnSize = this.preferredLayoutSize($container).subtract(graphics.insets($container));
      filterColumnSize = filterColumnSize.subtract(filterColumnMargins);
    }
    // width is always set with css
    filterColumnSize.width = $filterColumn.cssWidth();

    // Set explicit height, necessary if there is no filter table
    $filterColumn.cssHeight(filterColumnSize.height);

    // TODO [7.0] cgu this code could be written a lot easier -> replace following code (filter fields, filter table) with HtmlComponent.get($filterColumn).setSize(filterColumnSize);
    // Same for pref size. To implement max height of table, the RowLayout could read css max-height
    // Filter fields
    if (this.popup.hasFilterFields) {
      let
        $filterFieldsGroup = this.popup.$filterFieldsGroup,
        filterFieldHtmlComp = HtmlComponent.get($filterFieldsGroup.find('.form-field'));

      // Layout filter field(s) and get size
      filterFieldHtmlComp.setSize(new Dimension(filterColumnSize.width - filterColumnInsets.horizontal(), this._filterFieldsGroupBoxHeight()));
      filterFieldGroupSize = graphics.size($filterFieldsGroup, true);
    }

    // Filter table
    if (this.popup.hasFilterTable) {
      let
        filterTableContainerHeight,
        $filterTableGroup = this.popup.$filterTableGroup,
        filterTableContainerInsets = graphics.insets($filterTableGroup),
        filterTableHtmlComp = this.popup.filterTable.htmlComp;

      filterTableContainerHeight = filterColumnSize.height - filterColumnInsets.vertical();
      // subtract height of filter-fields container
      filterTableContainerHeight -= filterFieldGroupSize.height;
      // subtract group-title height
      filterTableContainerHeight -= this._groupTitleHeight($filterTableGroup);
      // subtract insets of table container
      filterTableContainerHeight -= filterTableContainerInsets.vertical();
      // limit height of table
      filterTableContainerHeight = Math.min(filterTableContainerHeight, TableHeaderMenuLayout.TABLE_MAX_HEIGHT);

      // Layout filter table
      filterTableHtmlComp.setSize(new Dimension(
        filterColumnSize.width - filterColumnInsets.horizontal(),
        filterTableContainerHeight));
    }

    // fix width of actions column, so it doesn't become wider when user
    // hovers over a button and thus the text of the group changes.
    this._setMaxWidth();
    actionColumnSize = graphics.size(this.popup.$columnActions);
    this._setMaxWidth(actionColumnSize.width);
  }

  _adjustSizeWithAnchor(prefSize) {
    let maxWidth,
      htmlComp = this.popup.htmlComp,
      windowPaddingX = this.popup.windowPaddingX,
      popupMargins = htmlComp.margins(),
      popupBounds = htmlComp.offsetBounds(),
      $window = this.popup.$container.window(),
      windowWidth = $window.width();

    maxWidth = (windowWidth - popupMargins.horizontal() - popupBounds.x - windowPaddingX);
    let compact = popupBounds.width > maxWidth;
    if (compact) {
      this.popup.$body.addClass('compact', compact);
      prefSize = this.preferredLayoutSize(this.popup.$container);
    }

    return super._adjustSizeWithAnchor(prefSize);
  }

  // group title (size used for table + field container)
  _groupTitleHeight($group) {
    return graphics.size($group.find('.table-header-menu-group-text'), true).height;
  }

  _filterFieldsGroupBoxHeight() {
    return this.popup.filterFieldsGroupBox.htmlComp.prefSize().height;
  }

  /**
   * The preferred layout size of this widget is
   * + size of table (but height is limited to TABLE_MAX_HEIGHT, if table becomes too large)
   * + size of filter-fields
   * + paddings of surrounding containers
   */
  preferredLayoutSize($container, options) {
    let prefSize, filterColumnMargins, filterColumnInsets,
      rightColumnHeight = 0,
      leftColumnHeight = 0,
      containerInsets = graphics.insets($container),
      oldMaxWidth = this._getMaxWidth();

    this._setMaxWidth(); // temp. remove max-width so we can determine pref. size
    leftColumnHeight = graphics.size(this.popup.$columnActions, true).height;

    // Filter table
    if (this.popup.hasFilterTable) {
      let
        $filterTableGroup = this.popup.$filterTableGroup,
        filterTableHeight = this.popup.filterTable.htmlComp.size(true).height,
        filterTableContainerInsets = graphics.insets($filterTableGroup),
        filterTableContainerHeight;

      // limit height of table
      filterTableHeight = Math.min(filterTableHeight, TableHeaderMenuLayout.TABLE_MAX_HEIGHT);
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
      let
        $filterFieldsGroup = this.popup.$filterFieldsGroup,
        filterFieldContainerInsets = graphics.insets($filterFieldsGroup),
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
      filterColumnMargins = graphics.margins(this.popup.$columnFilters);
      filterColumnInsets = graphics.insets(this.popup.$columnFilters);
      rightColumnHeight += filterColumnMargins.vertical();
      rightColumnHeight += filterColumnInsets.vertical();
    }

    // Use height of left or right column as preferred size (and add insets of container)
    prefSize = graphics.prefSize($container);
    if (!this.popup.$body.hasClass('compact')) {
      prefSize.height = Math.max(leftColumnHeight, rightColumnHeight) + containerInsets.vertical();
    } else {
      prefSize.height = leftColumnHeight + rightColumnHeight + containerInsets.vertical();
    }

    // restore max-width
    this._setMaxWidth(oldMaxWidth);

    return prefSize;
  }

  _getMaxWidth() {
    return this.popup.$columnActions.css('max-width');
  }

  _setMaxWidth(maxWidth) {
    this.popup.$columnActions.css('max-width', maxWidth || '');
  }
}
