/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DoEntity, Event, ModelAdapter, RemoteEvent, UiCallbackResponseEvent, UiCallbacks, Widget} from '../../../index';

export class UiCallbacksAdapter extends ModelAdapter {
  declare widget: UiCallbacks;

  override onModelAction(event: RemoteEvent) {
    if (event.type === 'uiCallback') {
      this._onModelUiCallback(event as UiCallbackRemoteEvent);
    } else {
      super.onModelAction(event);
    }
  }

  protected override _onWidgetEvent(event: Event<UiCallbacks>) {
    if (event.type === 'uiResponse') {
      this._onUiResponse(event as UiCallbackResponseEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onModelUiCallback(event: UiCallbackRemoteEvent) {
    let owner: Widget = null;
    if (event.owner) {
      let ownerAdapter = this.session.getModelAdapter(event.owner);
      if (!ownerAdapter) {
        throw new Error('Adapter could not be resolved. Id: ' + event.owner);
      }
      owner = ownerAdapter.widget;
    }
    this.widget.onUiCallbackRequest(event.handlerObjectType, event.id, owner, event.data);
  }

  protected _onUiResponse(event: UiCallbackResponseEvent) {
    let response = event.data;
    this._send('uiResponse', response);
  }
}

interface UiCallbackRemoteEvent<TObject extends DoEntity = DoEntity> extends RemoteEvent {
  id: string;
  handlerObjectType: string;
  owner: string;
  data: TObject;
}
