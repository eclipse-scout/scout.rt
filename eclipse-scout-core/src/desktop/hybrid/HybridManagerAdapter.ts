/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AnyDoEntity, dataObjects, Event, HybridActionEvent, HybridManager, hybridUtil, ModelAdapter, RemoteEvent} from '../../index';

export class HybridManagerAdapter extends ModelAdapter {
  declare widget: HybridManager;

  override onModelAction(event: RemoteEvent) {
    if (event.type === 'hybridEvent') {
      this._onHybridEvent(event as HybridRemoteEvent);
    } else if (event.type === 'hybridWidgetEvent') {
      this._onHybridWidgetEvent(event as HybridRemoteEvent);
    } else {
      super.onModelAction(event);
    }
  }

  protected _onHybridEvent(event: HybridRemoteEvent) {
    let contextElements = hybridUtil.jsonToContextElements(this.session, event.contextElements);
    let dataobject = dataObjects.deserialize(event.data, null, {createPojoIfDoIsUnknown: true});
    this.widget.onHybridEvent(event.id, event.eventType, dataobject, contextElements);
  }

  protected _onHybridWidgetEvent(event: HybridRemoteEvent) {
    let dataobject = dataObjects.deserialize(event.data, null, {createPojoIfDoIsUnknown: true});
    this.widget.onHybridWidgetEvent(event.id, event.eventType, dataobject);
  }

  protected override _onWidgetEvent(event: Event<HybridManager>) {
    if (event.type === 'hybridAction') {
      this._onWidgetHybridAction(event as HybridActionEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onWidgetHybridAction(event: HybridActionEvent) {
    let dataobject = dataObjects.serialize(event.data.data);
    let contextElements = hybridUtil.contextElementsToJson(event.data.contextElements);
    this._send('hybridAction', {
      actionType: event.data.actionType, // add as first property (devtools sometimes show properties in that order)
      id: event.data.id,
      data: dataobject,
      contextElements: contextElements || undefined
    });
  }
}

interface HybridRemoteEvent<TObject extends AnyDoEntity = AnyDoEntity> extends RemoteEvent {
  id: string;
  eventType: string;
  data: TObject;
  contextElements: Record<string, JsonHybridActionContextElement[]>;
}

export interface JsonHybridActionContextElement {
  /** adapter id */
  widget: string;
  /** widget-specific element representation */
  element?: any;
}
