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
scout.TableLayout = function(table) {
  scout.TableLayout.parent.call(this);
  this.table = table;
};
scout.inherits(scout.TableLayout, scout.AbstractLayout);

scout.TableLayout.prototype.layout = function($container) {
  var menuBarSize,
    menuBar = this.table.menuBar,
    footer = this.table.footer,
    header = this.table.header,
    $data = this.table.$data,
    lastColumn = this.table.columns[this.table.columns.length - 1],
    height = 0,
    htmlMenuBar = scout.HtmlComponent.get(menuBar.$container),
    htmlContainer = this.table.htmlComp,
    containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if (menuBar.$container.isVisible()) {
    menuBarSize = scout.MenuBarLayout.size(htmlMenuBar, containerSize);
    htmlMenuBar.setSize(menuBarSize);
    height += menuBarSize.height;
  }
  if (footer) {
    // Layout table footer and add size of footer (including the control content) to 'height'
    footer.htmlComp.revalidateLayout();
    height += scout.graphics.getSize(footer.$container).height;
    height += scout.graphics.getSize(footer.$controlContainer).height;
  }
  if (header) {
    height += scout.graphics.getSize(header.$container).height;
  }
  $data.css('height', 'calc(100% - ' + height + 'px)');

  if (this.table.autoResizeColumns) {
    this._layoutColumns();
  }

  // Size of last column may have to be adjusted due to the header menu items
  if (header) {
    header.resizeHeaderItem(lastColumn);
  }

  // Make sure tooltips and editor popup are at correct position after layouting (e.g after window resizing)
  this.table.tooltips.forEach(function(tooltip) {
    tooltip.position();
  }.bind(this));
  if (this.table.cellEditorPopup && this.table.cellEditorPopup.rendered) {
    this.table.cellEditorPopup.position();
    this.table.cellEditorPopup.pack();
  }
  scout.scrollbars.update(this.table.$data);
};

/**
 * Resizes the columns to make them use all the available space.
 */
scout.TableLayout.prototype._layoutColumns = function() {
  var newWidth, weight,
    relevantColumns = [],
    columns = this.table.columns,
    currentWidth = 0,
    totalInitialWidth = 0,
    minWidthAdjustment = 0,
    availableWidth = this.table.$data.outerWidth() - this.table.rowBorderWidth;

  columns.forEach(function(column) {
    if (column.fixedWidth) {
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

  // First resize columns which will get smaller than the minimum width
  columns = relevantColumns;
  columns.forEach(function(column) {
    if (totalInitialWidth === 0) {
      weight = 1 / relevantColumns.length;
    } else {
      weight = column.initialWidth / totalInitialWidth;
    }
    newWidth = weight * availableWidth;
    if (newWidth < column.minWidth) {
      newWidth = column.minWidth;
      minWidthAdjustment += newWidth;
      totalInitialWidth -= column.initialWidth;
      scout.arrays.remove(relevantColumns, column);
      this.table.resizeColumn(column, newWidth);
    }
  }.bind(this));
  availableWidth -= minWidthAdjustment;

  // Then resize the others
  relevantColumns.forEach(function(column) {
    if (totalInitialWidth === 0) {
      weight = 1 / relevantColumns.length;
    } else {
      weight = column.initialWidth / totalInitialWidth;
    }
    newWidth = weight * availableWidth;
    if (newWidth !== column.width) {
      this.table.resizeColumn(column, newWidth);
    }
  }.bind(this));
};
