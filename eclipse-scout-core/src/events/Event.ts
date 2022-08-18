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
import $ from 'jquery';
import {EventEmitter} from '../index';

export default class Event {
  source: EventEmitter;
  defaultPrevented: boolean;
  type: string;

  constructor(model?) {
    this.defaultPrevented = false;
    $.extend(this, model);
  }

  preventDefault() {
    this.defaultPrevented = true;
  }
}

