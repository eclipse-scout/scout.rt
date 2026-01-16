/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, dataObjects, Event, hybridUtil, JsonHybridActionContextElement, ModelAdapter, RemoteEvent, scout, UiCallbackEndEvent, UiCallbacks, Widget} from '../../../index';

export class UiCallbacksAdapter extends ModelAdapter {
  declare widget: UiCallbacks;

  override onModelAction(event: RemoteEvent) {
    if (event.type === 'callback') {
      this._onModelCallback(event as UiCallbackRemoteEvent);
    } else {
      super.onModelAction(event);
    }
  }

  protected _onModelCallback(event: UiCallbackRemoteEvent) {
    let callbackId = event.id;
    let handlerObjectType = event.handlerObjectType;
    let owner = this._resolveOwner(event.owner);
    let data = dataObjects.deserialize(event.data);
    let contextElements = hybridUtil.jsonToContextElements(this.session, event.contextElements);

    this.widget.onCallback(handlerObjectType, callbackId, owner, data, contextElements);
  }

  protected _resolveOwner(adapterId: string): Widget {
    if (!adapterId) {
      return null;
    }
    let adapter = scout.assertInstance(this.session.getModelAdapter(adapterId), ModelAdapter, `No adapter found for '${adapterId}'`);
    return scout.assertInstance(adapter.widget, Widget);
  }

  // ----------

  protected override _onWidgetEvent(event: Event<UiCallbacks>) {
    if (event.type === 'callbackEnd') {
      this._onWidgetCallbackEnd(event as UiCallbackEndEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onWidgetCallbackEnd(event: UiCallbackEndEvent) {
    let callbackId = event.callbackId;
    let error = event.error;
    let result = event.result;

    this._send('callbackEnd', {
      id: callbackId,
      error: dataObjects.serialize(error) ?? undefined,
      data: dataObjects.serialize(result?.data) ?? undefined,
      contextElements: hybridUtil.contextElementsToJson(result?.contextElements) ?? undefined
    });
  }
}

interface UiCallbackRemoteEvent<TObject extends BaseDoEntity = BaseDoEntity> extends RemoteEvent {
  id: string;
  handlerObjectType: string;
  owner: string;
  data: TObject;
  contextElements: Record<string, JsonHybridActionContextElement[]>;
}
