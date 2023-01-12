/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GridData, PropertyChangeEvent, Widget, WidgetEventMap} from '../index';

export interface CarouselEventMap extends WidgetEventMap {
  'propertyChange:currentItem': PropertyChangeEvent<number>;
  'propertyChange:gridData': PropertyChangeEvent<GridData>;
  'propertyChange:statusEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:widgets': PropertyChangeEvent<Widget[]>;
}
