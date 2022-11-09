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
import {DisplayParent, Event, FileChooser, PropertyChangeEvent, WidgetEventMap} from '../index';

export interface FileChooserEventMap extends WidgetEventMap {
  'cancel': Event<FileChooser>;
  'upload': Event<FileChooser>;
  'propertyChange:acceptTypes': PropertyChangeEvent<string>;
  'propertyChange:displayParent': PropertyChangeEvent<DisplayParent>;
  'propertyChange:files': PropertyChangeEvent<File[]>;
  'propertyChange:maximumUploadSize': PropertyChangeEvent<number>;
  'propertyChange:multiSelect': PropertyChangeEvent<boolean>;
}
