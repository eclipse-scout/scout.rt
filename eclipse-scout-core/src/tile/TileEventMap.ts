/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ColorScheme, GridData, PropertyChangeEvent, WidgetEventMap} from '../index';

export interface TileEventMap extends WidgetEventMap {
  'propertyChange:colorScheme': PropertyChangeEvent<ColorScheme>;
  'propertyChange:filterAccepted': PropertyChangeEvent<boolean>;
  'propertyChange:gridData': PropertyChangeEvent<GridData>;
  'propertyChange:gridDataHints': PropertyChangeEvent<GridData>;
  'propertyChange:selectable': PropertyChangeEvent<boolean>;
  'propertyChange:selected': PropertyChangeEvent<boolean>;
}
