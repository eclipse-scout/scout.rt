/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {PropertyChangeEvent, ValueFieldEventMap} from '../../../index';

export interface FileChooserButtonEventMap extends ValueFieldEventMap<File> {
  'propertyChange:acceptTypes': PropertyChangeEvent<string>;
  'propertyChange:fileExtensions': PropertyChangeEvent<string | string[]>;
  'propertyChange:iconId': PropertyChangeEvent<string>;
  'propertyChange:maximumUploadSize': PropertyChangeEvent<number>;
}
