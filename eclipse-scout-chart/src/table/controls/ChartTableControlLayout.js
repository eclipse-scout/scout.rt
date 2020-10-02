/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, scrollbars} from '@eclipse-scout/core';

export default class ChartTableControlLayout extends AbstractLayout {

  constructor(control) {
    super();
    this.control = control;
  }

  layout($container) {
    if (!this.control.contentRendered) {
      return;
    }
    scrollbars.update(this.control.$contentContainer);
  }
}
