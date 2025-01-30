/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HybridActionContextElementConverters, ModelAdapter} from '../../../index';

/**
 * Instances of this class are used to convert a model element to a JSON representation and vice versa. Each
 * instance typically only handles a specific type of element. Because elements have no common structure, they are
 * always accompanied by the widget that owns them (here represented by the corresponding {@link ModelAdapter}).
 *
 * How to add a new converter:
 * - Create a subclass of this abstract class and register it with {@link HybridActionContextElementConverters#register}.
 * - Specify the types of the generic parameters and implement the abstract methods {@link #_acceptAdapter}, {@link #_acceptJsonElement}
 *   and {@link #_acceptModelElement} accordingly. This is necessary because the type parameters only exist at compile-time.
 * - Implement the abstract methods {@link #_jsonToElement} and {@link #_elementToJson}. They are only called if the arguments were
 *   accepted by the corresponding methods. Otherwise, the task is delegated to the next converter.
 */
export abstract class HybridActionContextElementConverter<TAdapter extends ModelAdapter = ModelAdapter, TJsonElement = any, TModelElement = any> {

  /**
   * Tries to convert the given JSON representation of an element (e.g. a string id) to the corresponding model element.
   *
   * If the arguments are not supported by this converter, `null` is returned.
   */
  tryConvertFromJson(adapter: ModelAdapter, jsonElement: any): any {
    if (this._acceptAdapter(adapter) && this._acceptJsonElement(jsonElement)) {
      return this._jsonToElement(adapter, jsonElement);
    }
    return null;
  }

  /**
   * Tries to convert the given model element to a JSON representation (e.g. a string id).
   *
   * If the arguments are not supported by this converter, `null` is returned.
   */
  tryConvertToJson(adapter: ModelAdapter, modelElement: any): any {
    if (this._acceptAdapter(adapter) && this._acceptModelElement(modelElement)) {
      return this._elementToJson(adapter, modelElement);
    }
    return null;
  }

  /**
   * @return whether this instance can handle the given argument (otherwise, the task is delegated to the next converter)
   */
  protected abstract _acceptAdapter(adapter: ModelAdapter): adapter is TAdapter;

  /**
   * @return whether this instance can handle the given argument (otherwise, the task is delegated to the next converter)
   */
  protected abstract _acceptJsonElement(jsonElement: any): jsonElement is TJsonElement;

  /**
   * @return whether this instance can handle the given argument (otherwise, the task is delegated to the next converter)
   */
  protected abstract _acceptModelElement(modelElement: any): modelElement is TModelElement;

  /**
   * @param adapter owner widget (e.g. TreeAdapter)
   * @param jsonElement JSON representation of the element (e.g. String)
   * @return model representation of the element (e.g. TreeNode)
   */
  protected abstract _jsonToElement(adapter: TAdapter, jsonElement: TJsonElement): TModelElement;

  /**
   * @param adapter owner widget (e.g. TreeAdapter)
   * @param modelElement model representation of the element (e.g. TreeNode)
   * @return JSON representation of the element (e.g. String)
   */
  protected abstract _elementToJson(adapter: TAdapter, modelElement: TModelElement): TJsonElement;
}
