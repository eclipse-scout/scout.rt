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
import {scout} from '../index';

export default class UserAgent {

  constructor(model) {
    model = model || {};
    if (!model.deviceType) {
      throw new Error('deviceType needs to be defined');
    }
    this.deviceType = model.deviceType;
    this.touch = scout.nvl(model.touch, false);
    this.standalone = scout.nvl(model.standalone, false);
  }
}
