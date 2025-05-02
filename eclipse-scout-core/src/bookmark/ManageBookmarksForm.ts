/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, arrays, BookmarkDo, BookmarkForm, BookmarkStore, BookmarkSupport, Event, Form, FormModel, InitModelOf, ManageBookmarksFormWidgetMap, scout} from '../index';
import model from './ManageBookmarksFormModel';

export class ManageBookmarksForm extends Form {
  declare widgetMap: ManageBookmarksFormWidgetMap;
  declare data: BookmarkDo[];

  protected override _jsonModel(): FormModel {
    return model();
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.widget('EditMenu').on('action', this._onEditMenuAction.bind(this));
    this.widget('DeleteMenu').on('action', this._onDeleteMenuAction.bind(this));
    this.widget('ActivateMenu').on('action', this._onActivateMenuAction.bind(this));

    this._installUpDownMenus();
  }

  protected override _load(): JQuery.Promise<any> {
    return BookmarkStore.get(this.session).loadAllBookmarks();
  }

  override importData() {
    let rows = arrays.ensure(this.data).map(bookmark => {
      let name = bookmark.title;
      return {
        cells: [
          bookmark,
          name,
          null,
          null
        ]
      };
    });
    this.widget('BookmarksTable').insertRows(rows);
  }

  override exportData(): any {
    const table = this.widget('BookmarksTable');
    const bookmarkColumn = table.columnById('BookmarkColumn');

    return table.rows.map(row => bookmarkColumn.cellValue(row));
  }

  protected override _save(data: any): JQuery.Promise<void> {
    return BookmarkStore.get(this.session).storeAllBookmarks(data);
  }

  protected _onEditMenuAction(event: Event<Action>) {
    const table = this.widget('BookmarksTable');
    const bookmarkColumn = table.columnById('BookmarkColumn');
    const nameColumn = table.columnById('NameColumn');
    const selectedRow = table.selectedRow();

    let form = scout.create(BookmarkForm, {
      parent: this,
      bookmark: bookmarkColumn.cellValue(selectedRow)
    });
    form.open();
    form.whenSave().then(() => {
      let bookmark = form.data;
      let name = bookmark.title;
      bookmarkColumn.setCellValue(selectedRow, bookmark);
      nameColumn.setCellValue(selectedRow, name);
    });
  }

  protected _onDeleteMenuAction(event: Event<Action>) {
    const table = this.widget('BookmarksTable');

    table.deleteRows(table.selectedRows);
  }

  protected _onActivateMenuAction(event: Event<Action>) {
    const table = this.widget('BookmarksTable');
    const bookmarkColumn = table.columnById('BookmarkColumn');

    BookmarkSupport.get(this.session).activateBookmark(bookmarkColumn.cellValue(table.selectedRow()));
  }

  protected _installUpDownMenus() {
    const tableField = this.widget('BookmarksTableField');
    const table = this.widget('BookmarksTable');
    const moveRowUpMenu = table.widget('MoveRowUpMenu');
    const moveRowDownMenu = table.widget('MoveRowDownMenu');

    const onMoveRowMenuActionHandler = (event: Event<Action>) => {
      let moveUp = event.source === moveRowUpMenu;
      let moved = false;
      table.selectedRows.slice()
        .sort((r1, r2) => {
          if (moveUp) {
            return table.rows.indexOf(r1) - table.rows.indexOf(r2);
          }
          return table.rows.indexOf(r2) - table.rows.indexOf(r1); // reverse
        })
        .some(row => {
          let i1 = table.rows.indexOf(row);
          if (moveUp) {
            table.moveRowUp(row);
          } else {
            table.moveRowDown(row);
          }
          let i2 = table.rows.indexOf(row);
          if (i1 === i2) {
            return true; // stop if move did not do anything
          }
          moved = true;
          return false; // continue
        });
      if (moved) {
        tableField.touch();
      }
    };

    moveRowUpMenu.on('action', onMoveRowMenuActionHandler);
    moveRowDownMenu.on('action', onMoveRowMenuActionHandler);

    table.on('propertyChange:enabledComputed', event => this._recomputeUpDownMenuVisibility());
    table.on('rowsSelected rowsInserted rowsUpdated rowsDeleted rowOrderChanged', event => this._recomputeUpDownMenuVisibility());

    this._recomputeUpDownMenuVisibility();
  }

  protected _recomputeUpDownMenuVisibility() {
    const table = this.widget('BookmarksTable');
    const moveRowUpMenu = table.widget('MoveRowUpMenu');
    const moveRowDownMenu = table.widget('MoveRowDownMenu');

    let moveable = table.enabledComputed && table.selectedRows.length && table.rows.length > 1;
    let firstRow = arrays.first(table.visibleRows);
    let lastRow = arrays.last(table.visibleRows);
    moveRowUpMenu.setVisible(moveable);
    moveRowUpMenu.setEnabled(table.selectedRows.every(row => row !== firstRow));
    moveRowDownMenu.setVisible(moveable);
    moveRowDownMenu.setEnabled(table.selectedRows.every(row => row !== lastRow));
  }
}
