/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {focusUtils, keys, KeyStroke, ScoutKeyboardEvent} from '@eclipse-scout/core';
import {ChartTableControl} from './ChartTableControl';

/**
 * Focuses the first chart type if the focus is on the content container of the chart table control (which is the case when the table control opens).
 */
export class FocusFirstChartTypeKeyStroke extends KeyStroke {
  declare field: ChartTableControl;

  constructor(action: ChartTableControl) {
    super();
    this.field = action;
    this.which = [keys.DOWN];

    this.stopPropagation = true;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    if (!super._accept(event)) {
      return false;
    }
    return focusUtils.isActiveElement(this.field.$contentContainer);
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.$chartSelect.children('.chart-type').first()[0]?.focus();
  }
}
