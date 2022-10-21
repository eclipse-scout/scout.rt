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
import {Event, FormFieldEventMap, ImageField, PropertyChangeEvent} from '../../../index';

export interface ImageFieldFileUploadEvent<T = ImageField> extends Event<T> {
  file: File;
}

export default interface ImageFieldEventMap extends FormFieldEventMap {
  'fileUpload': ImageFieldFileUploadEvent;
  'propertyChange:autoFit': PropertyChangeEvent<boolean>;
  'propertyChange:imageUrl': PropertyChangeEvent<string>;
  'propertyChange:scrollBarEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:uploadEnabled': PropertyChangeEvent<boolean>;
}
