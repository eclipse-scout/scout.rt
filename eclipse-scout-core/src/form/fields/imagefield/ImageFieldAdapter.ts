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
import {Event, FormFieldAdapter, ImageField} from '../../../index';
import {ImageFieldFileUploadEvent} from './ImageFieldEventMap';

export default class ImageFieldAdapter extends FormFieldAdapter {
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
