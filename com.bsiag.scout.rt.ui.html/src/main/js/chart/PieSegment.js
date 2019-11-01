/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import * as $ from 'jquery';

export default class PieSegment {

constructor() {
  this.label = null;
  this.value = null;
  this.color = null;
  this.cssClass = null;
  this.valueGroup = null; // reference to the original group
  this.valueGroupIndex = null;
  this.clickable = true;
}

init(model) {
  $.extend(this, model);
}
}
