/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import $ from 'jquery';
import {Predicate, scout} from '../index';

export class RemoteEvent {
  target: string;
  type: string;
  showBusyIndicator?: boolean;
  coalesce?: Predicate<RemoteEvent>;
  newRequest?: boolean;
  properties?: Record<string, any>;

  [property: string]: any; // Data will be applied to the event directly

  constructor(target: string, type: string, data?: object) {
    scout.assertParameter('target', target);
    scout.assertParameter('type', type);
    $.extend(this, data);
    this.target = target;
    this.type = type;
  }
}
