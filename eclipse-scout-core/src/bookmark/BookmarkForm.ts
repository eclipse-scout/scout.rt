/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BookmarkDo, BookmarkFormModel, BookmarkFormWidgetMap, BookmarkStore, BookmarkSupport, Form, FormModel} from '../index';
import model from './BookmarkFormModel';

// noinspection GrazieInspection
/**
 * Form to create or edit a {@link BookmarkDo}.
 *
 * The following modes are supported:
 *
 * 1. Edit in-memory bookmark
 *
 *    When the form is started, the given bookmark is loaded without calling the bookmark store. When the form is
 *    saved, the bookmark is updated in-place without calling the bookmark store.
 *
 *    This mode is active when {@link bookmark} is set.
 *
 * 2. Edit bookmark from store:
 *
 *    When starting the form, the bookmark is loaded from the {@link BookmarkStore bookmark store} by its ID. When
 *    the form is saved, the bookmark is updated in the store.
 *
 *    This mode is active when {@link bookmarkId} is set.
 *
 * 3. Create new bookmark:
 *
 *    When the form is loaded, a new bookmark is created from the current state of the desktop. When the form is
 *    saved, the new bookmark is stored to the {@link BookmarkStore bookmark store}. Afterward, the stored bookmark
 *    is available in the {@link data} property.
 *
 *    This mode is active when neither {@link bookmarkId} nor {@link bookmark} are set.
 */
export class BookmarkForm extends Form implements BookmarkFormModel {
  declare model: BookmarkFormModel;
  declare widgetMap: BookmarkFormWidgetMap;
  declare data: BookmarkDo;

  bookmark: BookmarkDo;
  bookmarkId: string;

  protected override _jsonModel(): FormModel {
    return model();
  }

  protected override _load(): JQuery.Promise<any> {
    // 1. Edit in-memory bookmark
    if (this.bookmark) {
      return $.resolvedPromise(this.bookmark);
    }
    // 2. Edit bookmark from store
    if (this.bookmarkId) {
      return BookmarkStore.get(this.session).loadBookmark(this.bookmarkId);
    }
    // 3. Create new bookmark
    return BookmarkSupport.get(this.session).createBookmark();
  }

  override importData() {
    this.widget('NameField').setValue(this.data.title);
  }

  override exportData(): any {
    this.data.title = this.widget('NameField').value;
    return this.data;
  }

  protected override _save(data: any): JQuery.Promise<void> {
    if (this.bookmark) {
      return $.resolvedPromise(); // nop, bookmark was already updated in exportData()
    }
    return BookmarkStore.get(this.session).storeBookmark(this.data)
      .then(bookmark => this.setData(bookmark));
  }

  override ok(): JQuery.Promise<void> {
    if (!this.bookmarkId && !this.bookmark) {
      this.touch(); // Save always in "new" mode
    }
    return super.ok();
  }
}
