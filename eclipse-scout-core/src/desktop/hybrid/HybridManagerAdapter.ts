/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, HybridActionEvent, HybridEvent, HybridManager, ModelAdapter} from '../../index';

export class HybridManagerAdapter extends ModelAdapter {
  declare widget: HybridManager;

  constructor() {
    super();
  }

  override onModelAction(event: any) {
    if (event.type === 'hybridEvent') {
      this._onHybridEvent(event);
    } else if (event.type === 'hybridWidgetEvent') {
      this._onHybridWidgetEvent(event);
    } else {
      super.onModelAction(event);
    }
  }

  protected _onHybridEvent(event: HybridEvent) {
    this.widget.onHybridEvent(event.id, event.eventType, event.data);
  }

  protected _onHybridWidgetEvent(event: HybridEvent) {
    this.widget.onHybridWidgetEvent(event.id, event.eventType, event.data);
  }

  protected override _onWidgetEvent(event: Event<HybridManager>) {
    if (event.type === 'hybridAction') {
      this._onWidgetHybridAction(event as HybridActionEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onWidgetHybridAction(event: HybridActionEvent) {
    this._send('hybridAction', event.data);
  }
}
