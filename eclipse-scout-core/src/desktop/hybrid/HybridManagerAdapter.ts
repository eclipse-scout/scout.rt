/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, HybridActionContextElement, HybridActionContextElementConverter, HybridActionEvent, HybridManager, JsonHybridActionContextElement, ModelAdapter, RemoteEvent, scout} from '../../index';

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
    let contextElements = this._jsonToContextElements(event.contextElements);
    this.widget.onHybridEvent(event.id, event.eventType, event.data, contextElements);
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
      contextElements: this._contextElementsToJson(event.data.contextElements) || undefined,
      data: event.data.data
    });
  }

  protected _jsonToContextElements(jsonContextElements: object): Record<string, HybridActionContextElement> {
    if (!jsonContextElements) {
      return null;
    }
    let contextElements: Record<string, HybridActionContextElement> = {};
    Object.entries(jsonContextElements).forEach(([key, jsonContextElement]) => {
      contextElements[key] = this._jsonToContextElement(jsonContextElement);
    });
    return contextElements;
  }

  protected _jsonToContextElement(jsonContextElement: JsonHybridActionContextElement): HybridActionContextElement {
    if (!jsonContextElement) {
      return null;
    }

    let adapter = this.session.getModelAdapter(jsonContextElement.widget);
    if (!(adapter instanceof ModelAdapter)) {
      throw new Error(`No adapter found for '${jsonContextElement.widget}'`);
    }
    let element = null;
    if (jsonContextElement.element) {
      element = HybridActionContextElementConverter.convertFromJson(adapter, jsonContextElement.element);
      if (!element) {
        throw new Error('Unable to convert JSON to context element');
      }
    }

    return scout.create(HybridActionContextElement, {
      widget: adapter.widget,
      element: element
    });
  }

  protected _contextElementsToJson(contextElements: Record<string, HybridActionContextElement>): object {
    if (!contextElements) {
      return null;
    }
    let json: object = {};
    Object.entries(contextElements).forEach(([key, contextElement]) => {
      json[key] = this._contextElementToJson(contextElement);
    });
    return json;
  }

  protected _contextElementToJson(contextElement: HybridActionContextElement): JsonHybridActionContextElement {
    if (!contextElement) {
      return null;
    }

    let adapter = contextElement.widget.modelAdapter;
    if (!(adapter instanceof ModelAdapter)) {
      throw new Error('Widget does not have a model adapter');
    }

    let jsonElement = undefined;
    if (contextElement.element) {
      jsonElement = HybridActionContextElementConverter.convertToJson(adapter, contextElement.element);
      if (!jsonElement) {
        throw new Error('Unable to convert context element to JSON');
      }
    }

    return {
      widget: adapter.id,
      element: jsonElement
    };
  }
}

interface HybridEvent<TObject = object> extends RemoteEvent {
  id: string;
  eventType: string;
  data: TObject;
  contextElements: object;
}
