/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormField} from '../../index';
import $ from 'jquery';

export default class GridData {

  constructor(model) {
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

  static createFromHints(field, gridColumnCount) {
    let data = new GridData(field.gridDataHints);
    if (data.w === FormField.FULL_WIDTH) {
      data.w = gridColumnCount;
    }
    return data;
  }

  static ensure(gridData) {
    if (!gridData) {
      return gridData;
    }
    if (gridData instanceof GridData) {
      return gridData;
    }
    return new GridData(gridData);
  }
}
