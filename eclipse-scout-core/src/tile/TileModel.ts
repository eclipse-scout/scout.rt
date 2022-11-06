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
import {GridData, WidgetModel} from '../index';
import {TileDisplayStyle} from './Tile';
import {ColorScheme} from '../util/colorSchemes';

export default interface TileModel extends WidgetModel {
  /**
   * Specifies the color scheme of the tile.
   *
   * It may be a {@link ColorScheme} object or a string.
   * The string needs to consist of the color scheme id and an optional inverted identifier separated by -, e.g. default-inverted.
   *
   * By default, the non-inverted DEFAULT scheme is used, see {@link colorSchemes}.
   */
  colorScheme?: ColorScheme | string;
  /**
   * Specifies the display style of the tile. Default is {@link Tile.DisplayStyle.DEFAULT};
   */
  displayStyle?: TileDisplayStyle;
  /**
   * Specifies, how the tile should be positioned in the grid, if the container uses a {@link LogicalGrid}.
   */
  gridDataHints?: GridData;
  /**
   * Specifies, whether the tile is selected. Default is false.
   */
  selected?: boolean;
  /**
   * Specifies, whether the tile can be selected. Default is false.
   */
  selectable?: boolean;
}
