/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableLayout = function(table) {
  scout.TableLayout.parent.call(this);
  this.table = table;
  this._dataHeightPositive = false;
};
scout.inherits(scout.TableLayout, scout.AbstractLayout);

scout.TableLayout.prototype.layout = function($container) {
  var menuBarHeight = 0,
    footerHeight = 0,
    headerHeight = 0,
    tileTableHeight = 0,
    controlContainerHeight = 0,
    controlContainerInsets,
    tileGrid = this.table.mediator ? this.table.mediator.tileAccordion : null,
    $data = this.table.$data ? this.table.$data : tileGrid.$container,
    dataMargins = scout.graphics.margins($data),
    dataMarginsHeight = dataMargins.top + dataMargins.bottom,
    menuBar = this.table.menuBar,
    footer = this.table.footer,
    header = this.table.header,
    tileTableHeaderBox = this.table.tileTableHeaderBox,
    visibleColumns = this.table.visibleColumns(),
    lastColumn = visibleColumns[visibleColumns.length - 1],
    htmlContainer = this.table.htmlComp,
    containerSize = htmlContainer.availableSize({
      exact: true
    }).subtract(htmlContainer.insets());

  if (this.table.menuBarVisible && menuBar.visible) {
    var htmlMenuBar = scout.HtmlComponent.get(menuBar.$container);
    var menuBarSize = scout.MenuBarLayout.size(htmlMenuBar, containerSize);
    htmlMenuBar.setSize(menuBarSize);
    menuBarHeight = menuBarSize.height;
  }
  if (header) {
    headerHeight = scout.graphics.size(header.$container).height;
    if (header.menuBar) {
      header.menuBar.validateLayout();
    }
  }
  if (footer) {
    // Layout table footer and add size of footer (including the control content) to 'height'
    footerHeight = scout.graphics.size(footer.$container).height;
    controlContainerHeight = footer.computeControlContainerHeight(this.table, footer.selectedControl, !this._dataHeightPositive);
    controlContainerInsets = scout.graphics.insets(footer.$controlContainer);
    if (!footer.animating) { // closing or opening: height is about to be changed
      footer.$controlContainer.cssHeight(controlContainerHeight);
      footer.$controlContent.outerHeight(controlContainerHeight - controlContainerInsets.vertical());
      footer.revalidateLayout();
    }
  }
  if (tileTableHeaderBox && tileTableHeaderBox.visible) {
    var htmlTileTableHeaderBox = scout.HtmlComponent.get(tileTableHeaderBox.$container);
    var tileTableHeaderBoxSize = scout.GroupBoxLayout.size(htmlTileTableHeaderBox, containerSize);
    htmlTileTableHeaderBox.setSize(tileTableHeaderBoxSize);
    tileTableHeight = tileTableHeaderBoxSize.height;
    tileTableHeaderBox.revalidateLayout();
  }
  $data.css('height', 'calc(100% - ' + (dataMarginsHeight + menuBarHeight + controlContainerHeight + footerHeight + headerHeight + tileTableHeight) + 'px)');
  this._dataHeightPositive = $data.height() > 0;

  if (tileGrid) {
    tileGrid.revalidateLayout();
  }

  if (!this.table.tileMode) {

    this._layoutColumns();

    // Size of last column may have to be adjusted due to the header menu items
    if (header) {
      header.resizeHeaderItem(lastColumn);
    }

    this.table.setViewRangeSize(this.table.calculateViewRangeSize());

    if (!htmlContainer.layouted) {
      this.table._renderScrollTop();
    }

    // Always render viewport (not only when viewRangeSize changes), because view range depends on scroll position and data height
    this.table._renderViewport();

    // Make sure tooltips and editor popup are at correct position after layouting (e.g after window resizing)
    this.table.tooltips.forEach(function(tooltip) {
      if (tooltip.rendered) {
        tooltip.position();
      }
    }.bind(this));
    if (this.table.cellEditorPopup && this.table.cellEditorPopup.rendered) {
      this.table.cellEditorPopup.position();
      this.table.cellEditorPopup.pack();
    }

    this.table.updateScrollbars();
  }
};

scout.TableLayout.prototype._layoutColumns = function(widthHint) {
  this._autoOptimizeColumnsWidths();

  var htmlContainer = this.table.htmlComp;
  var columnLayoutDirty = this.table.columnLayoutDirty || !htmlContainer.sizeCached;
  if (!columnLayoutDirty) {
    var width = widthHint || htmlContainer.size().width;
    columnLayoutDirty = htmlContainer.sizeCached.width !== width;
  }
  // Auto resize only if table width or column structure has changed
  if (this.table.autoResizeColumns && columnLayoutDirty) {
    this._autoResizeColumns(widthHint);
    this.table.columnLayoutDirty = false;
  }
};

/**
 * Resizes all visible columns with autoOptimizeWidth set to true, if necessary (means if autoOptimizeWidthRequired is true)
 */
scout.TableLayout.prototype._autoOptimizeColumnsWidths = function() {
  this.table.visibleColumns().forEach(function(column) {
    if (column.autoOptimizeWidth && column.autoOptimizeWidthRequired) {
      this.table.resizeToFit(column, column.autoOptimizeMaxWidth);
    }
  }, this);
};

/**
 * Resizes the visible columns to make them use all the available space.
 */
scout.TableLayout.prototype._autoResizeColumns = function(widthHint) {
  var newWidth, weight,
    relevantColumns = [],
    currentWidth = 0,
    totalInitialWidth = 0,
    tableWidth = widthHint || this.table.$data.width(),
    availableWidth = Math.floor(tableWidth - this.table.rowBorderWidth);

  // Don't resize fixed and auto optimize width columns
  this.table.visibleColumns().forEach(function(column) {
    if (column.fixedWidth || column.autoOptimizeWidth) {
      availableWidth -= column.width;
    } else {
      relevantColumns.push(column);
      currentWidth += column.width;
      totalInitialWidth += column.initialWidth;
    }
  }.bind(this));

  if (availableWidth === currentWidth) {
    // Columns already use the available space, no need to resize
    return;
  }

  var remainingWidth = availableWidth;

  // First, filter columns which would get smaller than their minimal size
  var minWidthColumns = relevantColumns.filter(function(column) {
    // Use initial width as preferred width for auto resize columns.
    // This makes sure the column doesn't get too small on small screens. The user can still make the column smaller though.
    var minWidth = Math.max(column.minWidth, column.initialWidth);
    if (totalInitialWidth === 0) {
      weight = 1 / relevantColumns.length;
    } else {
      weight = column.initialWidth / totalInitialWidth;
    }
    newWidth = Math.floor(weight * remainingWidth);
    if (newWidth < minWidth) {
      newWidth = minWidth;
      remainingWidth = Math.max(remainingWidth - newWidth, 0);
      return true;
    }
    return false;
  }.bind(this));

  // Resize them to their minimal width
  minWidthColumns.forEach(function(column, index) {
    var minWidth = Math.max(column.minWidth, column.initialWidth);
    scout.arrays.remove(relevantColumns, column);

    newWidth = minWidth;
    totalInitialWidth -= column.initialWidth;
    // If this is the last column, add remaining space (due to rounding) to this column
    if (index === minWidthColumns.length - 1 && remainingWidth > 0 && relevantColumns.length === 0) {
      newWidth += remainingWidth;
      remainingWidth = 0;
    }
    if (newWidth !== column.width) {
      this.table.resizeColumn(column, newWidth);
    }
  }.bind(this));

  // Then resize the others
  availableWidth = remainingWidth;
  relevantColumns.forEach(function(column, index) {
    if (totalInitialWidth === 0) {
      weight = 1 / relevantColumns.length;
    } else {
      weight = column.initialWidth / totalInitialWidth;
    }
    newWidth = Math.floor(weight * availableWidth);
    remainingWidth -= newWidth;
    // If this is the last column, add remaining space (due to rounding) to this column
    if (index === relevantColumns.length - 1 && remainingWidth > 0) {
      newWidth += remainingWidth;
      remainingWidth = 0;
    }
    if (newWidth !== column.width) {
      this.table.resizeColumn(column, newWidth);
    }
  }.bind(this));
};

scout.TableLayout.prototype.preferredLayoutSize = function($container, options) {
  // If autoResizeColumns and text wrap is enabled, the height of the table depends on the width
  this._layoutColumns(options.widthHint);

  // If table was not visible during renderViewport, the rows are not rendered yet (see _renderViewport)
  // -> make sure rows are rendered otherwise preferred height cannot be determined
  this.table._renderViewport();
  return scout.TableLayout.parent.prototype.preferredLayoutSize.call(this, $container, options);
};
