/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AnyDoEntity, dataObjects, Event, HybridActionContextElement, HybridActionContextElementConverters, HybridActionContextElements, HybridActionEvent, HybridManager, ModelAdapter, RemoteEvent, scout, Widget} from '../../index';

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
    let contextElements = this._jsonToContextElements(event.contextElements);
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
    this._send('hybridAction', {
      actionType: event.data.actionType, // add as first property (devtools sometimes show properties in that order)
      id: event.data.id,
      data: dataobject,
      contextElements: this._contextElementsToJson(event.data.contextElements) || undefined
    });
  }

  protected _jsonToContextElements(jsonContextElements: Record<string, JsonHybridActionContextElement[]>): HybridActionContextElements {
    if (!jsonContextElements) {
      return null;
    }
    let contextElements = scout.create(HybridActionContextElements);
    Object.keys(jsonContextElements).forEach(key => {
      let list = this._jsonToContextElementList(jsonContextElements[key]);
      contextElements.withElements(key, list);
    });
    return contextElements;
  }

  protected _jsonToContextElementList(jsonContextElements: JsonHybridActionContextElement[]): HybridActionContextElement[] {
    if (!jsonContextElements) {
      return null;
    }
    let list: HybridActionContextElement[] = [];
    jsonContextElements.forEach(jsonContextElement => {
      let contextElement = this._jsonToContextElement(jsonContextElement);
      list.push(contextElement);
    });
    return list;
  }

  protected _jsonToContextElement(jsonContextElement: JsonHybridActionContextElement): HybridActionContextElement {
    if (!jsonContextElement) {
      return null;
    }
    let adapterId = jsonContextElement.widget;
    let adapter = scout.assertInstance(this.session.getModelAdapter(adapterId), ModelAdapter, `No adapter found for '${adapterId}'`);
    let widget = scout.assertInstance(adapter.widget, Widget);

    let jsonElement = jsonContextElement.element;
    let modelElement = this._jsonToModelElement(adapter, jsonElement);

    return HybridActionContextElement.of(widget, modelElement);
  }

  protected _jsonToModelElement(adapter: ModelAdapter, jsonElement: any): any {
    if (!jsonElement) {
      return null;
    }
    for (let converter of HybridActionContextElementConverters.all()) {
      let modelElement = converter.tryConvertFromJson(adapter, jsonElement);
      if (modelElement) {
        return modelElement;
      }
    }
    throw new Error(`Unable to convert JSON to model element [adapter=${adapter?.id}, jsonElement=${jsonElement}]`);
  }

  protected _contextElementsToJson(contextElements: HybridActionContextElements): Record<string, JsonHybridActionContextElement[]> {
    if (!contextElements) {
      return null;
    }
    let json = {};
    for (let [key, list] of contextElements.map) {
      json[key] = this._contextElementListToJson(list);
    }
    return json;
  }

  protected _contextElementListToJson(contextElements: HybridActionContextElement[]): JsonHybridActionContextElement[] {
    if (!contextElements) {
      return null;
    }
    return contextElements.map(contextElement => this._contextElementToJson(contextElement));
  }

  protected _contextElementToJson(contextElement: HybridActionContextElement): JsonHybridActionContextElement {
    if (!contextElement) {
      return null;
    }
    let adapter = scout.assertInstance(contextElement.widget.modelAdapter, ModelAdapter, 'Widget does not have a model adapter');
    let jsonElement = this._modelElementToJson(adapter, contextElement.element);

    return {
      widget: adapter.id,
      element: jsonElement || undefined
    };
  }

  protected _modelElementToJson(adapter: ModelAdapter, modelElement: any): any {
    if (!modelElement) {
      return null;
    }
    for (let converter of HybridActionContextElementConverters.all()) {
      let jsonElement = converter.tryConvertToJson(adapter, modelElement);
      if (jsonElement) {
        return jsonElement;
      }
    }
    throw new Error(`Unable to convert model element to JSON [adapter=${adapter?.id}, modelElement=${modelElement}]`);
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
