/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Status, strings} from '../index';
import $ from 'jquery';

export default class NotificationBadgeStatus extends Status {

  constructor(model) {
    super($.extend(true, {severity: Status.Severity.INFO}, model));
  }

  cssClass() {
    return strings.join(' ', Status.cssClassForSeverity(this.severity), 'notification-badge');
  }

  /**
   * @return {NotificationBadgeStatus} a clone of this Status instance.
   */
  clone() {
    let modelClone = $.extend({}, this);
    return new NotificationBadgeStatus(modelClone);
  }

  equals(o) {
    if (!(o instanceof NotificationBadgeStatus)) {
      return false;
    }
    return super.equals(o);
  }
}
