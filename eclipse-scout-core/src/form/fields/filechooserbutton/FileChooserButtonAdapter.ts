/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FileChooserButton, PropertyChangeEvent, ValueFieldAdapter} from '../../../index';

export class FileChooserButtonAdapter extends ValueFieldAdapter {
  declare widget: FileChooserButton;

  protected override _onWidgetPropertyChange(event: PropertyChangeEvent<any, FileChooserButton>) {
    super._onWidgetPropertyChange(event);

    if (event.propertyName === 'value') {
      this._onValueChange(event);
    }
  }

  protected _onValueChange(event: PropertyChangeEvent<File, FileChooserButton>) {
    let success = this.widget.fileInput.upload();
    if (!success) {
      this.widget.fileInput.clear();
    }
  }

  protected override _syncDisplayText(displayText: string) {
    this.widget.setDisplayText(displayText);
    // When displayText comes from the server we must not call parseAndSetValue here.
  }
}
