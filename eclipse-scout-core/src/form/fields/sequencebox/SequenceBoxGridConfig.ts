/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LogicalGridConfig, LogicalGridWidget, SequenceBox} from '../../../index';

export class SequenceBoxGridConfig extends LogicalGridConfig {

  declare widget: SequenceBox;

  override getGridColumnCount(): number {
    return this.widget.fields.length;
  }

  override getGridWidgets(): LogicalGridWidget[] {
    return this.widget.fields;
  }
}
