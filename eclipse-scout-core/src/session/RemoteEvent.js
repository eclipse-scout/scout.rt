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
import $ from 'jquery';
import {scout} from '../index';

export default class RemoteEvent {

  constructor(target, type, data) {
    scout.assertParameter('target', target);
    scout.assertParameter('type', type);
    $.extend(this, data);
    this.target = target;
    this.type = type;
  }
}
