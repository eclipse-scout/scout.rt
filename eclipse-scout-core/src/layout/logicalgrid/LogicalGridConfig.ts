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
export default class LogicalGridConfig {

  constructor() {
    this.widget = null;
  }

  setWidget(widget) {
    this.widget = widget;
  }

  getGridColumnCount() {
    return this.widget.gridColumnCount;
  }

  getGridWidgets() {
    return this.widget.widgets;
  }
}
