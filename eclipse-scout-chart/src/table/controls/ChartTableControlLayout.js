/*
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
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
    // ChartTableControl enlarges the content which would lead to scrollbars -> make them invisible while size is dirty
    if (!this.control.sizeDirty) {
      scrollbars.update(this.control.$contentContainer);
    }
  }
}
