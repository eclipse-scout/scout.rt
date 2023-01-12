/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, scrollbars} from '@eclipse-scout/core';
import {ChartTableControl} from '../../index';

export class ChartTableControlLayout extends AbstractLayout {
  control: ChartTableControl;

  constructor(control: ChartTableControl) {
    super();
    this.control = control;
  }

  override layout($container: JQuery) {
    if (!this.control.contentRendered) {
      return;
    }
    scrollbars.update(this.control.$contentContainer);
    scrollbars.update(this.control.$xAxisSelect);
    scrollbars.update(this.control.$yAxisSelect);
    scrollbars.update(this.control.$dataSelect);
  }
}
