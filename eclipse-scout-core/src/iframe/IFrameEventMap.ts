/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {PropertyChangeEvent, WidgetEventMap} from '../index';

export interface IFrameEventMap extends WidgetEventMap {
  'propertyChange:location': PropertyChangeEvent<string>;
  'propertyChange:sandboxEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:sandboxPermissions': PropertyChangeEvent<string>;
  'propertyChange:scrollBarEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:trackLocation': PropertyChangeEvent<boolean>;
}
