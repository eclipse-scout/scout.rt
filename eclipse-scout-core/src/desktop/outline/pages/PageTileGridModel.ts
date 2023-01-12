/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Outline, Page, TileGridLayoutConfig, TileGridModel} from '../../../index';

export interface PageTileGridModel extends TileGridModel {
  compact?: boolean;
  compactLayoutConfig?: TileGridLayoutConfig;
  outline?: Outline;
  page?: Page;
  nodes?: Page[];
}
