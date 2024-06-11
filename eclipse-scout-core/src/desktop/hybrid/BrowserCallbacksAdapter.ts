/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BrowserCallbackEvent, BrowserCallbacks, DoEntity, Event, ModelAdapter, RemoteEvent, Widget} from '../../index';

export class BrowserCallbacksAdapter extends ModelAdapter {
  declare widget: BrowserCallbacks;

  override onModelAction(event: RemoteEvent) {
    if (event.type === 'browserCallback') {
      this._onModelBrowserCallback(event as BrowserCallbackRemoteEvent);
    } else {
      super.onModelAction(event);
    }
  }

  protected override _onWidgetEvent(event: Event<BrowserCallbacks>) {
    if (event.type === 'browserResponse') {
      this._onBrowserResponse(event as BrowserCallbackEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onModelBrowserCallback(event: BrowserCallbackRemoteEvent) {
    let owner: Widget = null;
    if (event.owner) {
      let ownerAdapter = this.session.getModelAdapter(event.owner);
      if (!ownerAdapter) {
        throw new Error('Adapter could not be resolved. Id: ' + event.owner);
      }
      owner = ownerAdapter.widget;
    }
    this.widget.onBrowserCallbackRequest(event.handlerObjectType, event.id, owner, event.data);
  }

  protected _onBrowserResponse(event: BrowserCallbackEvent) {
    let response = event.data;
    this._send('browserResponse', response);
  }
}

interface BrowserCallbackRemoteEvent<TObject extends DoEntity = DoEntity> extends RemoteEvent {
  id: string;
  handlerObjectType: string;
  owner: string;
  data: TObject;
}
