/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, Status, strings} from '../index';
import $ from 'jquery';

export class NotificationBadgeStatus extends Status {

  constructor(model?: InitModelOf<Status>) {
    super($.extend(true, {severity: Status.Severity.INFO}, model));
  }

  override cssClass(): string {
    return strings.join(' ', Status.cssClassForSeverity(this.severity), 'notification-badge');
  }

  /**
   * @returns {NotificationBadgeStatus} a clone of this Status instance.
   */
  override clone(): Status {
    let modelClone = $.extend({}, this);
    return new NotificationBadgeStatus(modelClone);
  }

  override equals(o: any): boolean {
    if (!(o instanceof NotificationBadgeStatus)) {
      return false;
    }
    return super.equals(o);
  }
}
