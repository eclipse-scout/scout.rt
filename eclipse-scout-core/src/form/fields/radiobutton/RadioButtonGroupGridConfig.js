/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {LogicalGridConfig} from '../../../index';

export default class RadioButtonGroupGridConfig extends LogicalGridConfig {

  constructor() {
    super();
  }

  getGridColumnCount() {
    return this.widget.gridColumnCount;
  }

  getGridWidgets() {
    return this.widget.fields;
  }
}
