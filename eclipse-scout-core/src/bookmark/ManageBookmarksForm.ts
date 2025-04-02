/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, arrays, BookmarkDo, BookmarkForm, bookmarks, Event, Form, FormModel, InitModelOf, ManageBookmarksFormWidgetMap, scout, TableRowActionEvent} from '../index';
import model from './ManageBookmarksFormModel';

export class ManageBookmarksForm extends Form {
  declare widgetMap: ManageBookmarksFormWidgetMap;

  bookmarks: BookmarkDo[];
  deletedBookmarkIds: string[] = [];

  protected override _jsonModel(): FormModel {
    return model();
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.widget('EditMenu').on('action', this._onEditMenuAction.bind(this));
    this.widget('DeleteMenu').on('action', this._onDeleteMenuAction.bind(this));
    // FIXME bsh [js-bookmark] Move MoveRowMenu to Scout
    this.widget('MoveRowUpMenu').setVisibleGranted(false);
    this.widget('MoveRowDownMenu').setVisibleGranted(false);

    this.widget('BookmarksTable').on('rowAction', this._onTableRowAction.bind(this));
  }

  protected override _load(): JQuery.Promise<any> {
    return bookmarks.loadAllBookmarks('jswidgets:bookmarks')
      .then(bookmarks => {
        this.bookmarks = bookmarks;
        return super._load();
      });
  }

  override importData() {
    let rows = arrays.ensure(this.bookmarks).map(bookmark => {
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
    let table = this.widget('BookmarksTable');
    let bookmarkColumn = table.columnById('BookmarkColumn');
    this.bookmarks = table.rows.map(row => bookmarkColumn.cellValue(row));
    return null;
  }

  protected override _save(data: any): JQuery.Promise<void> {
    return bookmarks.storeAllBookmarks(this.bookmarks, 'jswidgets:bookmarks')
      .then(() => {
        this.findDesktop().trigger('bookmarksChanged');
        return super._save(data);
      });
  }

  protected _onEditMenuAction(event: Event<Action>) {
    this._editSelectedBookmark();
  }

  protected _onDeleteMenuAction(event: Event<Action>) {
    let table = this.widget('BookmarksTable');
    let bookmarkColumn = table.columnById('BookmarkColumn');
    this.deletedBookmarkIds.push(...table.selectedRows.map(row => bookmarkColumn.cellValue(row).id));
    table.deleteRows(table.selectedRows);
  }

  protected _onTableRowAction(event: TableRowActionEvent) {
    this._editSelectedBookmark();
  }

  _editSelectedBookmark() {
    let table = this.widget('BookmarksTable');
    let bookmarkColumn = table.columnById('BookmarkColumn');
    let selectedRow = table.selectedRow();

    let bookmark = bookmarkColumn.cellValue(selectedRow);
    let form = scout.create(BookmarkForm, {
      parent: this,
      bookmark: bookmark
    });
    form.open();
    form.whenSave().then(() => {
      let bookmark = form.bookmark;
      let name = bookmark.title;
      // FIXME bsh [js-bookmark] merge with importData()
      table.columnById('BookmarkColumn').setCellValue(selectedRow, bookmark);
      table.columnById('NameColumn').setCellValue(selectedRow, name);
    });
  }
}
