/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FileChooserButton, PropertyChangeEvent, ValueFieldAdapter} from '../../../index';

export default class FileChooserButtonAdapter extends ValueFieldAdapter {
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
