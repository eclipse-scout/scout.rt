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
import {Widget, WidgetModel} from '../index';

export default interface FileInputModel extends WidgetModel {
  /**
   * By default accept all
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
