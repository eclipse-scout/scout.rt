/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Widget, WidgetModel} from '../index';

export interface FileInputModel extends WidgetModel {
  /**
   * @see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/file#accept
   */
  acceptTypes?: string;
  /**
   * Default is {@link FileInput.DEFAULT_MAXIMUM_UPLOAD_SIZE}.
   */
  maximumUploadSize?: number;
  /**
   * Default is false.
   */
  multiSelect?: boolean;
  text?: string;
  uploadController?: Widget;
}
