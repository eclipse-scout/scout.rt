/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, BaseDoEntity, DraggableElement, Event, graphics, MoveData, MoveSupport, Point, Rectangle, scout, strings, Table, TableRow, typeName} from '@eclipse-scout/core';
import $ from 'jquery';

export class TableMoveSupport extends MoveSupport<DraggableTableRowElement> {
  declare widget: Table;
  declare _moveData: TableMoveData;

  override mouseMoveThreshold = 10;

  protected _cursorBackdropTimeoutId: number;

  constructor(table: Table) {
    super(table);
  }

  get table(): Table {
    return this.widget;
  }

  protected override _initMoveData(event: JQuery.MouseDownEvent, elements: DraggableTableRowElement[], draggedElement: DraggableTableRowElement) {
    super._initMoveData(event, elements, draggedElement);

    this._moveData.draggedElement = draggedElement;
    this._moveData.sourceRow = this._moveData.draggedElement.row;

    this._moveData.$dropIndicator = this._moveData.$container.appendDiv('tms-drop-indicator').setVisible(false);

    // Create a full-screen overlay to control the cursor inside and outside the table.
    // This also ensures that no other widgets react to the mouse while dragging rows.
    this._moveData.$cursorBackdrop = this._moveData.session.$entryPoint.appendDiv('tms-cursor-backdrop');
    this._moveData.$cursorBackdrop.on('wheel', event => {
      let $scrollable = this._moveData.$container;
      if (this._moveData.outside) {
        $scrollable = this._moveData.session.$entryPoint.elementFromPoint(event.pageX, event.pageY, ':scrollable');
      }
      $scrollable.trigger(event);
    });
    // Wait a short moment before making the cursor backdrop visible. That way, the mouse pointer stays unchanged
    // when the user just selects a row. This pause is also necessary for editable cells. If the backdrop was
    // shown immediately, it would capture all mouse events, including the 'mouseup' event needed to start the
    // cell editor (see Table#_onRowMouseUp).
    this._moveData.$cursorBackdrop.setVisible(false);
    this._cursorBackdropTimeoutId = setTimeout(() => {
      this._moveData.$cursorBackdrop.setVisible(true);
      // Force reflow to update cursor
      void this._moveData.$cursorBackdrop[0].offsetWidth;
      // After another short delay, automatically start dragging
      this._cursorBackdropTimeoutId = setTimeout(() => {
        this._moveData.moving = true;
        this._onFirstMouseMove();
        this._moveData.$window.trigger($.Event('mousemove', {
          pageX: event.pageX,
          pageY: event.pageY
        }));
      }, 750);
    }, 500);
  }

  protected override _onFirstMouseMove() {
    super._onFirstMouseMove();
    clearTimeout(this._cursorBackdropTimeoutId);
    this._moveData.$cursorBackdrop.setVisible(true);
    this._moveData.$cursorBackdrop.addClass('dragging');
  }

  // --------------------

  protected override _drag(event: JQuery.MouseMoveEvent) {
    this._moveData.$cursorBackdrop.removeClass('invalid-target');
    this._moveData.$cursorBackdrop.addClass('dragging');
    this._moveData.$clone.setVisible(true);

    let wasOutside = this._moveData.outside;
    this._moveData.outside = false;

    let rowX = this._moveData.containerBounds.x + this._moveData.startCursorPosition.x; // don't use pageX because rows might be shorter than the table
    let $rowBelowCursor = this._moveData.$container.elementFromPoint(rowX, event.pageY, '.table-row');
    let rowBelowCursor = $rowBelowCursor.data('row') as TableRow;

    let targetRow: TableRow;
    let placement: DraggedTableRowPlacement;
    let placementNoCenter: DraggedTableRowPlacement;

    // Compute the potential target position from the current mouse position. This position is later validated
    // and matched with the allowed drop types. The result is called "placement" and describes the position
    // of the drop indicator. When the mouse cursor is released, the current placement is converted into
    // a more convenient enum value called "drop position".
    if (rowBelowCursor) {
      targetRow = rowBelowCursor;

      let rowBounds = graphics.offsetBounds($rowBelowCursor);
      let rowMiddleY = rowBounds.y + rowBounds.height * 0.5;
      placementNoCenter = event.pageY < rowMiddleY ? 'above' : 'below';

      let rowMiddleY1 = rowBounds.y + Math.min(20, rowBounds.height * 0.25);
      let rowMiddleY2 = rowBounds.y + Math.min(20, rowBounds.height * 0.75);
      placement = event.pageY < rowMiddleY1 ? 'above' : (event.pageY > rowMiddleY2 ? 'below' : 'center');

      if (targetRow.expandable && targetRow.expanded) {
        if (placement === 'below') {
          placement = 'first-child';
        }
        if (placementNoCenter === 'below') {
          placementNoCenter = 'first-child';
        }
      }
    } else {
      // Before all rows
      let firstVisibleRow = arrays.first(this.table.visibleRows);
      let firstVisibleRowBounds = firstVisibleRow?.$row && graphics.offsetBounds(firstVisibleRow.$row);
      if (firstVisibleRowBounds && event.pageY < firstVisibleRowBounds.y) {
        placement = 'above';
        targetRow = arrays.first(this.table.visibleRootRows());
      } else {
        // After all rows
        let lastVisibleRow = arrays.last(this.table.visibleRows);
        let lastVisibleRowBounds = lastVisibleRow?.$row && graphics.offsetBounds(lastVisibleRow.$row);
        if (lastVisibleRowBounds && event.pageY >= lastVisibleRowBounds.bottom() && event.pageY < lastVisibleRowBounds.bottom() + 50) {
          placement = 'below';
          targetRow = arrays.last(this.table.visibleRootRows());
        }
      }
    }

    // Retrieve the allowed drop types for the current target row. By default, the drop types are taken
    // from the row's custom value "dropTypes". The table also emits an event, so consumers can compute
    // the drop types dynamically.
    if (this._moveData.targetRow !== targetRow) {
      let dropTypes: TableRowDropTypesDo;
      if (targetRow) {
        dropTypes = this.table.getAcceptedRowDropTypes(event, this._moveData.sourceRow, targetRow);
      }
      this._moveData.targetRowDropTypes = scout.create(TableRowDropTypesDo, {
        before: dropTypes ? (dropTypes.before || TableRowDropType.ALLOWED) : TableRowDropType.NONE,
        after: dropTypes ? (dropTypes.after || TableRowDropType.ALLOWED) : TableRowDropType.NONE,
        inside: dropTypes ? (dropTypes.inside || (this.table.hierarchical ? TableRowDropType.ALLOWED : TableRowDropType.NONE)) : TableRowDropType.NONE
      });
    }

    // Match placement with the allowed drop types.
    let dropType = this._moveData.dropType;
    let targetChanged = this._moveData.targetRow !== targetRow || this._moveData.placement !== placement;
    if (targetChanged) {
      let dropTypes = this._moveData.targetRowDropTypes;
      if (placement === 'center' && dropTypes.inside === TableRowDropType.NONE) {
        placement = placementNoCenter;
      }

      dropType = TableRowDropType.NONE;
      if (this._isValidPlacement(targetRow, placement)) {
        if (placement === 'above') {
          dropType = dropTypes.before;
        } else if (placement === 'below') {
          dropType = dropTypes.after;
        } else {
          dropType = dropTypes.inside;
        }
      }
    }

    targetChanged = targetChanged || this._moveData.dropType !== dropType;

    this._moveData.targetRow = targetRow;
    this._moveData.placement = placement;
    this._moveData.dropType = dropType;

    // Redraw drop indicator
    if (targetChanged || wasOutside) {
      this._updateDropIndicator();
    }
  }

  /**
   * Applies general rules for a placement to be valid that cannot be altered with custom drop types
   * (e.g. disallow dropping a row onto itself).
   */
  protected _isValidPlacement(targetRow: TableRow, placement: DraggedTableRowPlacement): boolean {
    if (!targetRow) {
      return false;
    }

    let sourceRow = this._moveData.sourceRow;

    if (this.table.hierarchical) {
      // Prevent moving row into its own subtree
      let row = targetRow.parentRow;
      while (row) {
        if (row === sourceRow) {
          return false; // target row is in subtree of source row
        }
        row = row.parentRow;
      }

      // Prevent moving row above its next sibling (even when there are child rows between them)
      if (placement === 'above') {
        let parentRows = sourceRow.parentRow
          ? this.table.visibleChildRows(sourceRow.parentRow)
          : this.table.visibleRootRows();
        let nextSiblingRow = parentRows[parentRows.indexOf(sourceRow) + 1];
        if (targetRow === nextSiblingRow) {
          return false;
        }
      }
    }

    // Compute the row just above and below the source row
    let prevRow: TableRow;
    let nextRow: TableRow;
    let sourceRowIndex = this.table.visibleRows.indexOf(sourceRow);
    if (sourceRowIndex > 0) {
      prevRow = this.table.visibleRows[sourceRowIndex - 1];
    }
    if (sourceRowIndex < this.table.visibleRows.length - 1) {
      nextRow = this.table.visibleRows[sourceRowIndex + 1];
    }

    // Prevent dropping row onto itself, except if levels are different (move row out of subtree)
    if (targetRow === sourceRow && (placement === 'center' || targetRow.hierarchyLevel === sourceRow.hierarchyLevel)) {
      return false;
    }
    // Prevent dropping row as first child of previous row
    if (targetRow === prevRow && placement === 'first-child') {
      return false;
    }
    // Prevent dropping row before previous row, except if target level is not the same (-> allow moving row into previous subtree)
    if (targetRow === prevRow && placement === 'below' && targetRow.hierarchyLevel === sourceRow.hierarchyLevel) {
      return false;
    }
    // Prevent dropping row above next row, except if target level is not the same (-> allow moving row out of subtree)
    if (targetRow === nextRow && placement === 'above' && targetRow.hierarchyLevel === sourceRow.hierarchyLevel) {
      return false;
    }

    return true;
  }

  protected override _dragOutside(event: JQuery.MouseMoveEvent) {
    this._moveData.outside = true;
    this._moveData.$cursorBackdrop.addClass('invalid-target');
    this._moveData.$clone.setVisible(false);
    this._moveData.$dropIndicator.setVisible(false);
  }

  protected _updateDropIndicator() {
    if (!this._moveData.targetRow?.$row || this._moveData.dropType === TableRowDropType.NONE) {
      // No drop target -> hide indicator
      this._moveData.$dropIndicator.setVisible(false);
      return;
    }

    // Show and reposition drop indicator
    this._moveData.$dropIndicator.setVisible(true);
    this._moveData.$dropIndicator.toggleClass('invalid', this._moveData.dropType === TableRowDropType.FORBIDDEN);

    let placement = this._moveData.placement;

    // Get row where the drop indicator is shown. Not necessarily the same as the target row. For example,
    // when the last root row has a subtree and the mouse cursor is below the last row, the target row
    // is the last _root_ row, but the indicator should bet drawn below the last _child_ row.
    let indicatorRow = this._moveData.targetRow;
    let indicatorLevel = indicatorRow.hierarchyLevel;
    if (placement === 'first-child') {
      indicatorLevel++;
    } else if (placement === 'below') {
      // Find last visible child row
      while (indicatorRow.expandable && indicatorRow.expanded) {
        indicatorRow = arrays.last(this.table.visibleChildRows(indicatorRow));
      }
    }

    let rowBounds = graphics.offsetBounds(indicatorRow.$row, {exact: true});
    let rowPosition = new Point(
      rowBounds.x - this._moveData.containerBounds.x + this.table.scrollLeft,
      rowBounds.y - this._moveData.containerBounds.y + this.table.scrollTop
    );

    let indent = indicatorLevel * this.table.rowLevelPadding;
    if (indicatorLevel && this.table.tableNodeColumn && placement !== 'center') {
      for (let column of this.table.visibleColumns()) {
        if (column === this.table.tableNodeColumn) {
          break;
        }
        indent += column.width;
      }
      indent += this.table.tableNodeColumn.tableNodeLevel0CellPadding;
    }

    this._moveData.$dropIndicator.cssLeft(rowPosition.x + indent);
    this._moveData.$dropIndicator.cssWidth(rowBounds.width - indent);
    this._moveData.$dropIndicator.cssTop(rowPosition.y + ((placement === 'below' || placement === 'first-child') ? rowBounds.height : 0));
    this._moveData.$dropIndicator.cssHeight(placement === 'center' ? rowBounds.height : '');
    this._moveData.$dropIndicator.toggleClass('center', placement === 'center');

    // Add classes to adjust position of indicator (ensure it does not enlarge the content, otherwise, scroll position might change)
    let beforeFirstRow = placement === 'above' && indicatorRow === arrays.first(this.table.visibleRows);
    this._moveData.$dropIndicator.toggleClass('above-first-row', !!beforeFirstRow);
    let afterLastRow = placement === 'below' && indicatorRow === arrays.last(this.table.visibleRows);
    this._moveData.$dropIndicator.toggleClass('below-last-row', !!afterLastRow);
  }

  // --------------------

  protected override _dragEnd(event: JQuery.MouseUpEvent): JQuery.Promise<Rectangle> {
    return $.resolvedPromise().then(async () => {
      // Immediately destroy clone, without animation
      this._moveData.$clone.remove();
      this._moveData.$clone = null;

      // Check if move is possible
      if (!this._moveData.targetRow || !this._moveData.placement || this._moveData.dropType !== TableRowDropType.ALLOWED || this._moveData.outside) {
        return;
      }

      // Delegate to table
      await this.table.dropRow(event, this._moveData.sourceRow, this._moveData.targetRow, this._getDropPosition());
    }).then(() => null);
  }

  /**
   * Convert the internal "placement" type to the enum {@link TableRowDropPosition}.
   */
  protected _getDropPosition(): TableRowDropPosition {
    let placement = this._moveData.placement;
    if (placement === 'above') {
      return TableRowDropPosition.BEFORE;
    }
    if (placement === 'below') {
      return TableRowDropPosition.AFTER;
    }
    if (placement === 'first-child') {
      return TableRowDropPosition.FIRST_CHILD;
    }
    if (placement === 'center') {
      return TableRowDropPosition.LAST_CHILD;
    }
    return null;
  }

  // --------------------

  protected override _cleanup() {
    clearTimeout(this._cursorBackdropTimeoutId);
    this._moveData.$cursorBackdrop.remove();
    this._moveData.$dropIndicator?.remove();
    super._cleanup();
  }

  protected override _append$Clone() {
    // Complete override: Instead of cloning the table row, we add a custom info element that looks like a tooltip.
    // It would be complicated to make the cloned row look good (e.g. we'd have to remove the selected state and
    // copy some css variables from the table). Also, it is easier to see where the drop indicator line is when
    // no element is directly below the mouse cursor.
    let $info = this._moveData.session.$entryPoint.appendDiv('tms-drag-info');
    $info.appendDiv('arrow');
    let $content = $info.appendDiv('content');
    let rowText = this._computeRowText(this._moveData.sourceRow);
    if (rowText) {
      $content.appendDiv('text').text(rowText);
      // FIXME bsh [dnd-table]: add support for multiple rows
      // $content.appendDiv('text small').text('und 2 weitere Zeilen');
    } else {
      $content.appendDiv('text').text('1 Zeile'); // FIXME bsh [dnd-table]: NLS
    }

    let cursorX = this._moveData.containerBounds.x + this._moveData.startCursorPosition.x;
    let cursorY = this._moveData.containerBounds.y + this._moveData.startCursorPosition.y;
    this._moveData.$clone = $info;
    this._moveData.cloneBounds = new Rectangle(cursorX, cursorY, 0, 0);
    this._moveData.cloneStartOffset = this._moveData.cloneBounds.point();
  }

  protected override _update$Clone() {
    // Complete override: scale and transform not necessary or supported
    this._moveData.$clone.cssPosition(this._moveData.cloneBounds.point());
  }

  // --------------------

  /**
   * Computes a text representing the given row. By default, the cell texts of all summary columns
   * are concatenated. If no summary columns are defined, the first visible column that provides
   * a text is used.
   */
  protected _computeRowText(row: TableRow): string {
    let summaryColumns = this.table.summaryColumns();
    if (arrays.hasElements(summaryColumns)) {
      return strings.join(' ', ...summaryColumns.map(summaryColumn => summaryColumn.cellText(row)));
    }
    for (let column of this.table.visibleColumns()) {
      if (!column.nodeColumnCandidate) {
        continue; // ignore special gui-only columns
      }
      // Explicitly cast text to String, because callText() might return other types (see Column#_formatValue)-
      // Some "empty" values would not be falsy otherwise, e.g. an empty array.
      let text = String(this.table.cellText(column, row));
      if (text) {
        return text;
      }
    }
    return null;
  }
}

// --------------------

export interface TableMoveData extends MoveData<DraggableTableRowElement> {
  $dropIndicator: JQuery;
  $cursorBackdrop: JQuery;
  outside: boolean;

  sourceRow: TableRow;
  targetRow: TableRow;
  placement: DraggedTableRowPlacement; // computed from cursor position and targetRowDropTypes
  dropType: TableRowDropType; // computed drop type for the target row and the active placement
  targetRowDropTypes: TableRowDropTypesDo; // updated when targetRow changes
}

export interface DraggableTableRowElement extends DraggableElement {
  row: TableRow;
}

/**
 * Target position represented by the current drop indicator.
 *
 * - `above` = source row is inserted before the target row.
 * - `below` = source row is inserted after the target row on the same level (i.e. child rows are skipped).
 * - `center` = source row is inserted in the target row's subtree at the end.
 * - `first-child` = source row is inserted in the target row's subtree at the start.
 */
export type DraggedTableRowPlacement = 'above' | 'below' | 'center' | 'first-child';

// --------------------

@typeName('scout.TableRowDropTypes')
export class TableRowDropTypesDo extends BaseDoEntity {
  /**
   * Default is {@link TableRowDropType#ALLOWED}
   */
  before: TableRowDropType;
  /**
   * Default is {@link TableRowDropType#ALLOWED}
   */
  after: TableRowDropType;
  /**
   * Default is {@link TableRowDropType#NONE} for flat tables and {@link TableRowDropType#ALLOWED} for hierarchical tables
   */
  inside: TableRowDropType;

  static of(dropType: TableRowDropType): TableRowDropTypesDo {
    return scout.create(TableRowDropTypesDo, {
      before: dropType,
      after: dropType,
      inside: dropType
    });
  }
}

/**
 * @see "TableRowDropType.java"
 */
export enum TableRowDropType {
  ALLOWED = 'allowed',
  FORBIDDEN = 'forbidden',
  NONE = 'none'
}

/**
 * @see "TableRowDropPosition.java"
 */
export enum TableRowDropPosition {
  /**
   * Move source row directly before target row (same level).
   */
  BEFORE = 'before',
  /**
   * Move source row directly after target row (same level).
   */
  AFTER = 'after',
  /**
   * Move source row into the subtree of target row as first direct child.
   */
  FIRST_CHILD = 'first-child',
  /**
   * Move source row into the subtree of target row as last direct child.
   */
  LAST_CHILD = 'last-child'
}

// --------------------

export class TableAcceptRowDropEvent extends Event<Table> implements TableAcceptRowDropEventModel {
  mouseEvent: JQuery.MouseMoveEvent;
  sourceRow: TableRow;
  targetRow: TableRow;

  /**
   * Can be modified or replaced by a new object.
   * If set to null, no drops are accepted ({@link TableRowDropType#NONE}).
   */
  dropTypes: TableRowDropTypesDo;

  constructor(model: TableAcceptRowDropEventModel) {
    super();
    $.extend(this, model);
  }

  setDropType(dropType: TableRowDropType) {
    this.dropTypes = TableRowDropTypesDo.of(dropType);
  }

  setDropTypes(dropTypes: TableRowDropTypesDo) {
    this.dropTypes = dropTypes;
  }
}

export interface TableAcceptRowDropEventModel {
  mouseEvent: JQuery.MouseMoveEvent;
  sourceRow: TableRow;
  targetRow: TableRow;
  dropTypes?: TableRowDropTypesDo;
}

export class TableRowDropEvent extends Event<Table> implements TableRowDropEventModel {
  mouseEvent: JQuery.MouseUpEvent;
  sourceRow: TableRow;
  targetRow: TableRow;
  position: TableRowDropPosition;

  /**
   * When promise is **resolved**:
   * Row order is changed and operation ends successfully.
   * This is the default.
   *
   * When promise is **rejected**:
   * Row order is not changed and operation is ended.
   * This is the same as calling `event.preventDefault()`.
   */
  promise: Promise<void>;

  constructor(model: TableRowDropEventModel) {
    super();
    $.extend(this, model);
  }

  setPromise(promise: Promise<void>) {
    this.promise = promise;
  }
}

export interface TableRowDropEventModel {
  mouseEvent: JQuery.MouseUpEvent;
  sourceRow: TableRow;
  targetRow: TableRow;
  position: TableRowDropPosition;
}
