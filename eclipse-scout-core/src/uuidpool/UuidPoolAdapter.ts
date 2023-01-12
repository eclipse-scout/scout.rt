/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Event, ModelAdapter, UuidPool, UuidPoolRefillEvent} from '../index';

export class UuidPoolAdapter extends ModelAdapter {
  declare widget: UuidPool;

  refillInProgress: boolean;

  constructor() {
    super();

    this.refillInProgress = false;
  }

  protected override _onWidgetEvent(event: Event<UuidPool>) {
    if (event.type === 'refill') {
      this._onWidgetRefill(event as UuidPoolRefillEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onWidgetRefill(event: UuidPoolRefillEvent) {
    // Prevent too many refill requests
    if (this.refillInProgress) {
      return;
    }
    this.refillInProgress = true;
    this._send('refill', {
      count: event.count
    }, {
      showBusyIndicator: false
    });
  }

  override onModelAction(event: any) {
    if (event.type === 'refill') {
      this._onModelRefill(event);
    } else {
      super.onModelAction(event);
    }
  }

  protected _onModelRefill(event: any) {
    arrays.pushAll(this.widget.uuids, event.uuids);
    this.refillInProgress = false;
  }
}
