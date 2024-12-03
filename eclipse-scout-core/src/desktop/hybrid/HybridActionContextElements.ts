/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, HybridActionContextElement, ObjectWithType, scout, Widget} from '../../index';

/**
 * Represents a map which holds lists of {@link HybridActionContextElement}s indexed by a string key. The value of the
 * map is always a list, even when only a single context element is expected. Several convenience methods to add or
 * retrieve context elements are provided, though the actual map can also be accessed via {@link map}.
 */
export class HybridActionContextElements implements ObjectWithType {

  objectType: string;

  map: Map<string, HybridActionContextElement[]> = new Map();

  /**
   * @returns whether the internal map is empty
   */
  isEmpty(): boolean {
    return this.map.size === 0;
  }

  /**
   * Returns a list of context elements for the given key. If the given key does not exist, an error is thrown.
   * Use {@link optList} to return `null` instead. Use {@link getSingle} if only a single context element is expected.
   */
  getList(key: string): HybridActionContextElement[] {
    return scout.assertValue(this.optList(key), `Missing context elements with key "${key}"`);
  }

  /**
   * Returns a list of context elements for the given key, or `null` if the given key does not exist.
   */
  optList(key: string): HybridActionContextElement[] {
    return this.map.get(key);
  }

  /**
   * Returns the context elements for the given key. If the given key does not exist, an error is thrown.
   * Use {@link optSingle} to return `null` instead. Use {@link getList} if more than one context element is expected.
   */
  getSingle(key: string): HybridActionContextElement {
    return scout.assertValue(this.optSingle(key), `Missing context element with key "${key}"`);
  }

  /**
   * Returns the context elements for the given key, or `null` if the given key does not exist.
   * Use {@link optList} if more than one context element is expected.
   */
  optSingle(key: string): HybridActionContextElement {
    return arrays.first(this.optList(key));
  }

  // ------------------------------

  /**
   * Stores the given map of context elements in the internal map. Nothing happens if the argument is `null` or empty.
   */
  withMap(map: Record<string, HybridActionContextElement[]>): HybridActionContextElements {
    scout.assertParameter('map', map);
    Object.keys(map).forEach(key => {
      this.map.set(key, map[key]);
    });
    return this;
  }

  /**
   * Stores the given list of context elements in the internal map under the given key. Existing elements for the same
   * key are overwritten.
   *
   * @throws Error if any of the arguments is `null`
   */
  withElements(key: string, contextElements: HybridActionContextElement[]): HybridActionContextElements {
    scout.assertParameter('key', key);
    scout.assertParameter('contextElements', contextElements);
    this.map.set(key, contextElements);
    return this;
  }

  /**
   * Stores the given context element in the internal map under the given key. Existing elements for the same key are
   * overwritten. If a widget is given as the second argument, it is automatically wrapped in a {@link HybridActionContextElement}.
   *
   * @throws Error if the mandatory arguments are `null`
   */
  withElement(key: string, contextElement: HybridActionContextElement): HybridActionContextElements;
  withElement(key: string, widget: Widget, element?: any): HybridActionContextElements;
  withElement(key: string, contextElementOrWidget: HybridActionContextElement | Widget, element?: any): HybridActionContextElements {
    scout.assertParameter('key', key);
    let contextElement = contextElementOrWidget instanceof HybridActionContextElement
      ? contextElementOrWidget
      : HybridActionContextElement.of(contextElementOrWidget, element);
    this.map.set(key, [contextElement]);
    return this;
  }
}
