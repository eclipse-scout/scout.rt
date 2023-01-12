/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, FormFieldAdapter, ImageField, ImageFieldFileUploadEvent} from '../../../index';

export class ImageFieldAdapter extends FormFieldAdapter {
  declare widget: ImageField;

  protected override _onWidgetEvent(event: Event<ImageField>) {
    if (event.type === 'fileUpload') {
      this._onFileUpload(event as ImageFieldFileUploadEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onFileUpload(event: ImageFieldFileUploadEvent) {
    let success = this.widget.fileInput.upload();
    if (!success) {
      this.widget.fileInput.clear();
    }
  }
}
