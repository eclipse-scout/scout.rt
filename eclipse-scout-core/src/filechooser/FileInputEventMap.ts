/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, FileInput, PropertyChangeEvent, WidgetEventMap} from '../index';

export interface FileInputChangeEvent<F extends FileInput = FileInput> extends Event<F> {
  files: File[];
}

export interface FileInputEventMap extends WidgetEventMap {
  'change': FileInputChangeEvent;
  'propertyChange:acceptTypes': PropertyChangeEvent<string>;
  'propertyChange:maximumUploadSize': PropertyChangeEvent<number>;
  'propertyChange:multiSelect': PropertyChangeEvent<boolean>;
  'propertyChange:text': PropertyChangeEvent<string>;
}
