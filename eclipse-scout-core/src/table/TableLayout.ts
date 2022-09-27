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
import {AbstractLayout, arrays, Dimension, graphics, HtmlComponent, MenuBarLayout, RowLayout, scout, scrollbars} from '../index';

export default class TableLayout extends AbstractLayout {

  constructor(table) {
    super();
    this.table = table;
    this._dataHeightPositive = false;
  }

  layout($container) {
    let menuBarHeight = 0,
      footerHeight = 0,
      headerHeight = 0,
      tileTableHeight = 0,
      controlContainerHeight = 0,
      controlContainerInsets,
      tileAccordion = this.table.tableTileGridMediator ? this.table.tableTileGridMediator.tileAccordion : null,
      $data = this.table.$data,
      dataMargins = graphics.margins(scout.nvl($data, this.table.$container)),
      dataMarginsHeight = dataMargins.top + dataMargins.bottom,
      menuBar = this.table.menuBar,
      footer = this.table.footer,
      header = this.table.header,
      tileTableHeader = this.table.tileTableHeader,
      visibleColumns = this.table.visibleColumns(),
      lastColumn = visibleColumns[visibleColumns.length - 1],
      htmlContainer = this.table.htmlComp,
      containerSize = htmlContainer.availableSize({
        exact: true
      }).subtract(htmlContainer.insets());

    if (this.table.menuBarVisible && menuBar.visible) {
      let htmlMenuBar = HtmlComponent.get(menuBar.$container);
      let menuBarSize = MenuBarLayout.size(htmlMenuBar, containerSize);
      htmlMenuBar.setSize(menuBarSize);
      menuBarHeight = menuBarSize.height;
    }
    $container.css('--menubar-height', menuBarHeight + 'px');
    if (header) {
      headerHeight = graphics.size(header.$container).height;
      if (header.menuBar) {
        header.menuBar.validateLayout();
      }
    }
    if (footer) {
      // Layout table footer and add size of footer (including the control content) to 'height'
      footerHeight = graphics.size(footer.$container).height;
      controlContainerHeight = footer.computeControlContainerHeight(this.table, footer.selectedControl, !this._dataHeightPositive);
      controlContainerInsets = graphics.insets(footer.$controlContainer);
      if (!footer.animating) { // closing or opening: height is about to be changed
        footer.$controlContainer.cssHeight(controlContainerHeight);
        footer.$controlContent.outerHeight(controlContainerHeight - controlContainerInsets.vertical());
        footer.revalidateLayout();
      }
    }
    if (tileTableHeader && tileTableHeader.visible) {
      let groupBoxSize = tileTableHeader.htmlComp.prefSize({
        widthHint: containerSize.width
      });
      groupBoxSize.width = containerSize.width;
      groupBoxSize = groupBoxSize.subtract(tileTableHeader.htmlComp.margins());
      tileTableHeader.htmlComp.setSize(groupBoxSize);
      tileTableHeight = groupBoxSize.height;
    }
    let controlsHeight = dataMarginsHeight + menuBarHeight + controlContainerHeight + footerHeight + headerHeight + tileTableHeight;
    let dataHeight = containerSize.height - controlsHeight;
    $container.css('--controls-height', controlsHeight + 'px');
    if ($data) {
      $data.css('height', 'calc(100% - ' + controlsHeight + 'px)');
      this._dataHeightPositive = $data.height() > 0;
    } else {
      if (tileAccordion && tileAccordion.htmlComp) {
        tileAccordion.htmlComp.setSize(new Dimension(containerSize.width, dataHeight));
        scrollbars.update(tileAccordion.$container);
        this._dataHeightPositive = dataHeight > 0;
      }
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

      // Render scroll top again to make sure the data is really at the correct position after rendering viewport.
      // Somehow table.$data[0].scrollTop changes during _renderViewport sometimes (e.g. when there are aggregate rows)
      if (!htmlContainer.layouted) {
        this.table._renderScrollTop();
      }

      // Make sure tooltips and editor popup are at correct position after layouting (e.g after window resizing)
      this.table.tooltips.forEach(tooltip => {
        if (tooltip.rendered) {
          tooltip.position();
        }
      });
      if (this.table.cellEditorPopup && this.table.cellEditorPopup.rendered) {
        this.table.cellEditorPopup.position();
        this.table.cellEditorPopup.pack();
      }

      this.table.updateScrollbars();
    }
  }

  _layoutColumns(widthHint) {
    this._autoOptimizeColumnsWidths();

    let htmlContainer = this.table.htmlComp;
    let columnLayoutDirty = this.table.columnLayoutDirty || !htmlContainer.sizeCached;
    if (!columnLayoutDirty) {
      let width = widthHint || htmlContainer.size().width;
      columnLayoutDirty = htmlContainer.sizeCached.width !== width;
    }
    // Auto resize only if table width or column structure has changed
    if (columnLayoutDirty) {
      if (this.table.autoResizeColumns) {
        this._autoResizeColumns(widthHint);
      }
      // This is already done in _renderRowsInRange, but it is necessary here as well if the zoom level changes dynamically (or autoResizeColumns toggles)
      this._updateRealColumnWidths();
      this.table.columnLayoutDirty = false;
    }
  }

  /**
   * Workaround for Chrome bug, see {@link Table._updateRealColumnWidths}
   */
  _updateRealColumnWidths() {
    if (this.table._updateRealColumnWidths()) {
      this.table._updateRowWidth();
      if (this.table.header && this.table.header.rendered) {
        this.table.header.resizeHeaderItems();
      }
      this.table.$rows(true)
        .css('width', this.table.rowWidth);
    }
  }

  /**
   * Resizes all visible columns with autoOptimizeWidth set to true, if necessary (means if autoOptimizeWidthRequired is true)
   */
  _autoOptimizeColumnsWidths() {
    this.table.visibleColumns().forEach(function(column) {
      if (column.autoOptimizeWidth && column.autoOptimizeWidthRequired) {
        this.table.resizeToFit(column, column.autoOptimizeMaxWidth);
      }
    }, this);
  }

  /**
   * Resizes the visible columns to make them use all the available space.
   */
  _autoResizeColumns(widthHint) {
    let newWidth, weight,
      relevantColumns = [],
      currentWidth = 0,
      totalInitialWidth = 0,
      tableWidth = widthHint || this.table.$data.width(),
      availableWidth = Math.floor(tableWidth - (this.table.rowBorders.horizontal() + this.table.rowMargins.horizontal()));

    // Don't resize fixed and auto optimize width columns
    this.table.visibleColumns().forEach(column => {
      if (column.fixedWidth || column.autoOptimizeWidth) {
        availableWidth -= column.width;
      } else {
        relevantColumns.push(column);
        currentWidth += column.width;
        totalInitialWidth += column.initialWidth;
      }
    });

    if (availableWidth === currentWidth) {
      // Columns already use the available space, no need to resize
      return;
    }

    let remainingWidth = availableWidth;

    // First, filter columns which would get smaller than their minimal size
    let minWidthColumns = relevantColumns.filter(column => {
      // Use initial width as preferred width for auto resize columns.
      // This makes sure the column doesn't get too small on small screens. The user can still make the column smaller though.
      let minWidth = Math.max(column.minWidth, column.initialWidth);
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
    });

    // Resize them to their minimal width
    minWidthColumns.forEach((column, index) => {
      let minWidth = Math.max(column.minWidth, column.initialWidth);
      arrays.remove(relevantColumns, column);

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
    });

    // Then resize the others
    availableWidth = remainingWidth;
    relevantColumns.forEach((column, index) => {
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
    });
  }

  preferredLayoutSize($container, options) {
    if (this.table.tileMode) {
      // Use RowLayout to calculate preferredLayoutSize of TileTableHeader, TileAccordion and Footer.
      return new RowLayout().preferredLayoutSize($container, options);
    }
    // If autoResizeColumns and text wrap is enabled, the height of the table depends on the width
    this._layoutColumns(options.widthHint);

    // If table was not visible during renderViewport, the rows are not rendered yet (see _renderViewport)
    // -> make sure rows are rendered otherwise preferred height cannot be determined
    this.table._renderViewport();
    return super.preferredLayoutSize($container, options);
  }
}
