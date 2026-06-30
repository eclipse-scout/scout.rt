/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, arrays, BookmarkDo, BookmarkForm, BookmarkStore, BookmarkSupport, Event, Form, FormModel, InitModelOf, ManageBookmarksFormWidgetMap, MoveTableRowMenuHelper, scout} from '../index';
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

    BookmarkSupport.get(this.session).activateBookmark(bookmarkColumn.selectedCellValue());
  }

  protected _installUpDownMenus() {
    const tableField = this.widget('BookmarksTableField');
    const table = this.widget('BookmarksTable');
    const moveRowUpMenu = table.widget('MoveRowUpMenu');
    const moveRowDownMenu = table.widget('MoveRowDownMenu');
    const moveRowHelper = scout.create(MoveTableRowMenuHelper);
    moveRowHelper.install({table, moveRowUpMenu, moveRowDownMenu});
    table.on('rowOrderChanged', () => tableField.touch());
  }
}
