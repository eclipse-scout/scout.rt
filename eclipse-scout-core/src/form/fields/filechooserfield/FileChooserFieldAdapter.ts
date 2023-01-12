/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FileChooserField, PropertyChangeEvent, ValueFieldAdapter} from '../../../index';

export class FileChooserFieldAdapter extends ValueFieldAdapter {
  declare widget: FileChooserField;

  static PROPERTIES_ORDER = ['value', 'displayText'];

  protected override _onWidgetPropertyChange(event: PropertyChangeEvent<any, FileChooserField>) {
    super._onWidgetPropertyChange(event);

    if (event.propertyName === 'value') {
      this._onValueChange(event);
    }
  }

  protected _onValueChange(event: PropertyChangeEvent<File, FileChooserField>) {
    let success = this.widget.fileInput.upload();
    if (!success) {
      this.widget.fileInput.clear();
    }
  }

  protected override _syncDisplayText(displayText: string) {
    this.widget.setDisplayText(displayText);
    // When displayText comes from the server we must not call parseAndSetValue here.
  }

  /**
   * Handle events in this order value, displayText. This allows to set a null value and set a display-text
   * anyway. Otherwise the field would be empty. Note: this order is not a perfect solution for every case,
   * but it solves the issue reported in ticket #290908.
   */
  protected override _orderPropertyNamesOnSync(newProperties: Record<string, any>): string[] {
    return Object.keys(newProperties).sort(this._createPropertySortFunc(FileChooserFieldAdapter.PROPERTIES_ORDER));
  }
}
