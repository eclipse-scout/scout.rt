/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HtmlEnvironment, LogicalGridLayout, LogicalGridLayoutConfig, ObjectOrModel, RadioButtonGroup} from '../../../index';

export class RadioButtonGroupLayout extends LogicalGridLayout {
  declare widget: RadioButtonGroup<any>;

  constructor(widget: RadioButtonGroup<any>, layoutConfig: ObjectOrModel<LogicalGridLayoutConfig>) {
    super(widget, layoutConfig);
  }

  protected override _initDefaults() {
    super._initDefaults();
    this.hgap = HtmlEnvironment.get().smallColumnGap;
  }
}
