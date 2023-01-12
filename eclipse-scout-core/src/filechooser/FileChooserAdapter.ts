/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, FileChooser, ModelAdapter} from '../index';

export class FileChooserAdapter extends ModelAdapter {
  declare widget: FileChooser;

  protected _onWidgetCancel(event: Event<FileChooser>) {
    // Do not close the file chooser immediately, server will send the close event
    event.preventDefault();
    this._send('cancel');
  }

  protected override _onWidgetEvent(event: Event<FileChooser>) {
    if (event.type === 'cancel') {
      this._onWidgetCancel(event);
    } else if (event.type === 'upload') {
      this._onUpload(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onUpload(event: Event<FileChooser>) {
    if (this.widget.rendered) {
      this.widget.uploadButton.setEnabled(false);
    }

    if (this.widget.files.length === 0) {
      return;
    }

    this.session.uploadFiles(this, this.widget.files, undefined, this.widget.maximumUploadSize);

    this.session.listen().done(() => {
      if (this.widget && this.widget.rendered) {
        this.widget.uploadButton.setEnabled(true);
      }
    });
  }
}
