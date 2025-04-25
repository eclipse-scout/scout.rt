/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HybridActionContextElement, HybridActionContextElementConverters, HybridActionContextElements, JsonHybridActionContextElement, ModelAdapter, scout, Session, Widget} from '../../index';

export class HybridUtil {

  jsonToContextElements(session: Session, jsonContextElements: Record<string, JsonHybridActionContextElement[]>): HybridActionContextElements {
    scout.assertParameter('session', session);
    if (!jsonContextElements) {
      return null;
    }
    let contextElements = scout.create(HybridActionContextElements);
    Object.keys(jsonContextElements).forEach(key => {
      let list = this._jsonToContextElementList(session, jsonContextElements[key]);
      contextElements.withElements(key, list);
    });
    return contextElements;
  }

  protected _jsonToContextElementList(session: Session, jsonContextElements: JsonHybridActionContextElement[]): HybridActionContextElement[] {
    if (!jsonContextElements) {
      return null;
    }
    let list: HybridActionContextElement[] = [];
    jsonContextElements.forEach(jsonContextElement => {
      let contextElement = this._jsonToContextElement(session, jsonContextElement);
      list.push(contextElement);
    });
    return list;
  }

  protected _jsonToContextElement(session: Session, jsonContextElement: JsonHybridActionContextElement): HybridActionContextElement {
    if (!jsonContextElement) {
      return null;
    }
    let adapterId = jsonContextElement.widget;
    let adapter = scout.assertInstance(session.getModelAdapter(adapterId), ModelAdapter, `No adapter found for '${adapterId}'`);
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

  // -----------------------------

  contextElementsToJson(contextElements: HybridActionContextElements): Record<string, JsonHybridActionContextElement[]> {
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

export const hybridUtil = scout.create(HybridUtil);
