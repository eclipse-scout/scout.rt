/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, FormFieldEventMap, ImageField, PropertyChangeEvent} from '../../../index';

export interface ImageFieldFileUploadEvent<T = ImageField> extends Event<T> {
  file: File;
}

export interface ImageFieldEventMap extends FormFieldEventMap {
  'fileUpload': ImageFieldFileUploadEvent;
  'propertyChange:autoFit': PropertyChangeEvent<boolean>;
  'propertyChange:imageUrl': PropertyChangeEvent<string>;
  'propertyChange:scrollBarEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:uploadEnabled': PropertyChangeEvent<boolean>;
}
