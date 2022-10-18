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
import {HtmlEnvironment, LogicalGridLayout, LogicalGridLayoutConfig, RadioButtonGroup} from '../../../index';
import {LogicalGridLayoutConfigModel} from '../../../layout/logicalgrid/LogicalGridLayoutConfig';

export default class RadioButtonGroupLayout extends LogicalGridLayout {
  declare widget: RadioButtonGroup<any>;

  constructor(widget: RadioButtonGroup<any>, layoutConfig: LogicalGridLayoutConfig | LogicalGridLayoutConfigModel) {
    super(widget, layoutConfig);
  }

  protected override _initDefaults() {
    super._initDefaults();
    this.hgap = HtmlEnvironment.get().smallColumnGap;
  }
}
