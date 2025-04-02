/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BookmarkDo, BookmarkFormModel, BookmarkFormWidgetMap, Form, FormModel} from '../index';
import model from './BookmarkFormModel';

export class BookmarkForm extends Form implements BookmarkFormModel {
  declare model: BookmarkFormModel;
  declare widgetMap: BookmarkFormWidgetMap;

  bookmark: BookmarkDo;

  protected override _jsonModel(): FormModel {
    return model();
  }

  override importData() {
    if (this.bookmark) {
      this.widget('NameField').setValue(this.bookmark.title);
    }
  }

  override exportData(): any {
    this.bookmark.title = this.widget('NameField').value;
    return null;
  }
}
