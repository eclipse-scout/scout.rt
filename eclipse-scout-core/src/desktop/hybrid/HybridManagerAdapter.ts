/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, HybridActionContextElement, HybridActionContextElementDissolver, HybridActionEvent, HybridManager, ModelAdapter, RemoteEvent} from '../../index';

export class HybridManagerAdapter extends ModelAdapter {
  declare widget: HybridManager;

  override onModelAction(event: RemoteEvent) {
    if (event.type === 'hybridEvent') {
      this._onHybridEvent(event as HybridEvent);
    } else if (event.type === 'hybridWidgetEvent') {
      this._onHybridWidgetEvent(event as HybridEvent);
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
    this._send('hybridAction', {
      id: event.data.id,
      actionType: event.data.actionType,
      contextElement: this._contextElementToJson(event.data?.contextElement),
      data: event.data.data
    });
  }

  protected _contextElementToJson(contextElement: HybridActionContextElement): object {
    if (!contextElement) {
      return undefined;
    }
    let dissolved = HybridActionContextElementDissolver.dissolve(contextElement);
    if (!dissolved) {
      throw new Error('Unable to convert context element to JSON');
    }
    return dissolved;
  }
}

interface HybridEvent<TObject = object> extends RemoteEvent {
  id: string;
  eventType: string;
  data: TObject;
}
