/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BookmarkDo, BookmarkFormModel, BookmarkFormWidgetMap, Form, FormModel, scout} from '../index';
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
      let language = this.session.locale.language; // FIXME bsh [js-bookmark] Use LanguageCodeType
      let name = this.bookmark.titles?.[language];
      this.widget('NameField').setValue(name);
    }
  }

  override exportData(): any {
    let language = this.session.locale.language; // FIXME bsh [js-bookmark] Use LanguageCodeType
    let titles = {
      [language]: this.widget('NameField').value
    };
    this.bookmark = scout.create(BookmarkDo, $.extend({}, this.bookmark, {
      titles: titles
    }));
    return null;
  }
}
