/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {PropertyChangeEvent, Status, WidgetEventMap} from '../../../index';

export interface TabEventMap extends WidgetEventMap {
  'propertyChange:errorStatus': PropertyChangeEvent<Status>;
  'propertyChange:label': PropertyChangeEvent<string>;
  'propertyChange:marked': PropertyChangeEvent<boolean>;
  'propertyChange:overflown': PropertyChangeEvent<boolean>;
  'propertyChange:selected': PropertyChangeEvent<boolean>;
  'propertyChange:subLabel': PropertyChangeEvent<string>;
  'propertyChange:tabbable': PropertyChangeEvent<boolean>;
  'propertyChange:tooltipText': PropertyChangeEvent<string>;
}
