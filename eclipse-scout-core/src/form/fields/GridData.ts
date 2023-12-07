/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Alignment, FormField, LogicalGridWidget, objects} from '../../index';
import $ from 'jquery';

export class GridData {
  /**
   * The horizontal position inside the grid. If set to -1, it will be calculated automatically by the {@link LogicalGrid}, which is the preferred way.
   *
   * Default is -1.
   */
  x?: number;
  /**
   * The vertical position inside the grid. If set to -1, it will be calculated automatically by the {@link LogicalGrid}, which is the preferred way.
   *
   * Default is -1.
   */
  y?: number;
  /**
   * The logical width of the grid cell. E.g. if the grid has two columns, set w to 2, so it spans both columns.
   * It is also used as min width if the grid cell should scale, see {@link weightX}.
   *
   * Default is 1.
   */
  w?: number;
  /**
   * The logical height of the grid cell. E.g. if the grid has two rows, set h to 2, so it spans both rows.
   * It is also used as min height if the grid cell should scale, see {@link weighty}.
   *
   * Default is 1.
   */
  h?: number;
  /**
   * Configures how much a grid cell should horizontally grow or shrink.<br>
   *
   * The value for this property can either be -1 or between 0 and 1.
   *
   * - 0 means fixed width and the grid cell won't grow or shrink.
   * - Greater 0 means the grid cell will grab the excess horizontal space and therefore grow or shrink. If the container
   *   contains more than one element with weightX > 0, the weight is used to specify how strong the width of the grid
   *   cell should be adjusted.
   * - -1 means the ui computes the optimal value so that the elements proportionally grab the excess space.
   *
   * *Examples*:
   * - A group box with 3 columns contains 3 fields: Every field has gridW = 1 and weightX = -1. This leads to 1 row
   * and 3 grid cells which would grow and shrink proportionally because weightX is automatically set to > 0.
   * - If the weight of these 3 fields were set to 0.1, 0.1 and 1, the first two fields would adjust the size very
   * slowly and would mostly be as big as a logical grid column (because gridW is set to 1), whereas the third field
   * would adjust its size very fast.
   *
   * Default is -1.
   */
  weightX?: number;
  /**
   * Configures how much a grid cell should vertically grow or shrink.
   *
   * The value for this property can either be -1 or between 0 and 1.
   *
   * - 0 means fixed height and the grid cell won't grow or shrink.
   * - Greater 0 means the grid cell will grab the excess vertical space and therefore grow or shrink. If the container
   *   contains more than one element with weightY > 0, the weight is used to specify how strong the height of the grid
   *   cell should be adjusted.
   * - -1 means the ui computes the optimal value so that the elements proportionally grab the excess space, but only if
   *   gridH is > 1. If gridH is 1 a weight of 0 is set and the grid cell does not grow or shrink.
   *
   * *Examples*:
   * - A group box with 1 column contains 3 fields: Every field has gridH = 1 and weightY = -1. This leads to 3 rows
   *   with fixed height, no additional space is grabbed, because weightY will automatically be set to 0.
   * - If the weight of these 3 fields were set to 1, the fields would grow and shrink proportionally.
   * - If the weight of these 3 fields were set to 0.1, 0.1 and 1, the first two fields would adjust the size very
   *   slowly and would mostly be a as big as one logical grid row (because gridH is set to 1), whereas the third field
   *   would adjust it's size very fast.
   *
   *   Default is -1.
   */
  weightY?: number;
  /**
   * Configures whether the element should be as width as preferred by the UI.
   * If the element has children, the preferred width normally is the computed width of the children.
   *
   * This property typically has less priority than {@link widthInPixel} and therefore only has an effect if no explicit width is set.
   *
   * Default is false.
   */
  useUiWidth?: boolean;
  /**
   * Configures whether the element should be as height as preferred by the UI.
   * If the element has children, the preferred height normally is the computed height of the children.
   *
   * This property typically has less priority than {@link heightInPixel} and therefore only has an effect if no explicit height is set.
   *
   * Default is false.
   * */
  useUiHeight?: boolean;
  /**
   * Configures the horizontal alignment of the element inside a grid cell, if {@link fillHorizontal} is set to false.
   *
   * Default is -1.
   */
  horizontalAlignment?: Alignment;
  /**
   * Configures the vertical alignment of the element inside a grid cell, if {@link fillVertical} is set to false.
   *
   * Default is -1.
   */
  verticalAlignment?: Alignment;
  /**
   * Configures whether the element should horizontally fill the grid cell.
   *
   * - If the property is set to true, the element takes all the horizontal space and therefore is as width as the grid cell.
   * - If it's set to false, the width is computed based on the properties {@link useUiWidth and {@link widthInPixel}.
   * - If non of these are set, a default value is used which typically is the width of a logical grid column.
   *
   * Default is true.
   */
  fillHorizontal?: boolean;
  /**
   * Configures whether the element should vertically fill the grid cell.
   *
   * - If the property is set to true, the element takes all the vertical space and therefore is as height as the grid cell.
   * - If it's set to false, the height is computed based on the properties {@link useUiHeight} and {@link heightInPixel}.
   * - If none of these are set, a default value is used which typically is the* height of a logical grid row.
   *
   * Default is true.
   */
  fillVertical?: boolean;
  /**
   * Configures the preferred width of the element in pixel.
   *
   * If the value is <=0 the property has no effect.
   *
   * Default is 0.
   */
  widthInPixel?: number;
  /**
   * Configures the preferred height of the element in pixel.
   *
   * If the value is <=0 the property has no effect.
   *
   * Default is 0.
   */
  heightInPixel?: number;

  constructor(model?: GridData) {
    model = model || {};
    this.x = -1;
    this.y = -1;
    this.w = 1;
    this.h = 1;
    this.weightX = -1;
    this.weightY = -1;
    this.useUiWidth = false;
    this.useUiHeight = false;
    this.horizontalAlignment = -1;
    this.verticalAlignment = -1;
    this.fillHorizontal = true;
    this.fillVertical = true;
    this.widthInPixel = 0;
    this.heightInPixel = 0;

    $.extend(this, model);
  }

  equals?(other: GridData) {
    if (!other || !(other instanceof GridData)) {
      return false;
    }
    return objects.propertiesEquals(this, other, Object.keys(this));
  }

  static createFromHints(field: LogicalGridWidget, gridColumnCount?: number): GridData {
    let data = new GridData(field.gridDataHints);
    if (data.w === FormField.FULL_WIDTH) {
      data.w = gridColumnCount;
    }
    return data;
  }

  static ensure(gridData: GridData): GridData {
    if (!gridData) {
      return gridData as GridData;
    }
    if (gridData instanceof GridData) {
      return gridData;
    }
    return new GridData(gridData);
  }
}
