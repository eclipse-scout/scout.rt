/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Outline, Page, PropertyChangeEvent, TileGridEventMap, TileGridLayoutConfig} from '../../../index';

export interface PageTileGridEventMap extends TileGridEventMap {
  'propertyChange:compact': PropertyChangeEvent<boolean>;
  'propertyChange:compactLayoutConfig': PropertyChangeEvent<TileGridLayoutConfig>;
  'propertyChange:nodes': PropertyChangeEvent<Page[]>;
  'propertyChange:outline': PropertyChangeEvent<Outline>;
  'propertyChange:page': PropertyChangeEvent<Page>;
}
