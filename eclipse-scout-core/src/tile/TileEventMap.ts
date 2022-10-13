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
import {GridData, PropertyChangeEvent, WidgetEventMap} from '../index';
import {ColorScheme} from '../util/colorSchemes';

export default interface TileEventMap extends WidgetEventMap {
  'propertyChange:colorScheme': PropertyChangeEvent<ColorScheme>;
  'propertyChange:filterAccepted': PropertyChangeEvent<boolean>;
  'propertyChange:gridDataHints': PropertyChangeEvent<GridData>;
  'propertyChange:selectable': PropertyChangeEvent<boolean>;
  'propertyChange:selected': PropertyChangeEvent<boolean>;
}
