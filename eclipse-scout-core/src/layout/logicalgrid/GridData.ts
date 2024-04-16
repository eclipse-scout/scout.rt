/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Alignment, FormField, GridDataModel, InitModelOf, LogicalGridWidget, ObjectOrModel, objects} from '../../index';
import $ from 'jquery';

export class GridData implements GridDataModel {
  declare model: GridDataModel;

  x: number;
  y: number;
  w: number;
  h: number;
  weightX: number;
  weightY: number;
  useUiWidth: boolean;
  useUiHeight: boolean;
  horizontalAlignment: Alignment;
  verticalAlignment: Alignment;
  fillHorizontal: boolean;
  fillVertical: boolean;
  widthInPixel: number;
  heightInPixel: number;

  constructor(model?: InitModelOf<GridData>) {
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

  /**
   * @returns a clone of this grid data enriched with the properties of the given model.
   */
  clone(model?: InitModelOf<GridData>): GridData {
    return new GridData($.extend({}, this, model));
  }

  equals(other: GridData): boolean {
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

  static ensure(gridData: ObjectOrModel<GridData>): GridData {
    if (!gridData) {
      return gridData as GridData;
    }
    if (gridData instanceof GridData) {
      return gridData;
    }
    return new GridData(gridData);
  }
}
