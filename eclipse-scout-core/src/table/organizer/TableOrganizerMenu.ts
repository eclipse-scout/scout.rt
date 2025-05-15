/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormMenu, InitModelOf, Popup, scout, TableOrganizerForm, WidgetModel} from '../../index';
import TableOrganizerMenuModel from './TableOrganizerMenuModel';

export class TableOrganizerMenu extends FormMenu {
  declare form: TableOrganizerForm;

  protected override _jsonModel(): WidgetModel {
    return TableOrganizerMenuModel();
  }

  override init(model: InitModelOf<this>) {
    super.init(model);
    this._updateForm();
    this.on('propertyChange:selected', event => this._updateForm());
  }

  protected _updateForm() {
    if (!this.selected) {
      // Destroy the form to not preserve the state of the tables (selection etc.)
      this.form?.destroy();
      return;
    }
    if (!this.form) {
      // Create the form when the menu is selected the first time
      this.setForm(scout.create(TableOrganizerForm, {
        parent: this
      }));
    }
    this.form.load();
  }

  protected override _createPopup(): Popup {
    const popup = super._createPopup();
    popup.addCssClass('table-organizer-menu-popup');
    return popup;
  }
}
