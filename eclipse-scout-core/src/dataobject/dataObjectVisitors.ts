/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {arrays, BaseDoEntity, Constructor, objects, scout, strings, TreeVisitResult} from '../index';

export const dataObjectVisitors = {

  /**
   * Visits all nodes and calls on each element matching the given type the provided consumer.
   * If a node matches, child nodes of this node are not visited.
   */
  forEach<T>(root: any, type: Constructor<T>, consumer: (node: T) => void) {
    dataObjectVisitors.forEachIf(root, (type ? c => c instanceof type : c => true) as (candidate) => candidate is T, consumer);
  },

  /**
   * Visits all nodes and calls on each element matching the given type the provided consumer.
   * If a node matches, child nodes of this node are also visited.
   */
  forEachRec<T>(root: any, type: Constructor<T>, consumer: (node: T) => void) {
    dataObjectVisitors.forEachIfRec(root, (type ? c => c instanceof type : c => true) as (candidate) => candidate is T, consumer);
  },

  /**
   * Visits all nodes and calls on each element matching the given type the provided visitor.
   * If a node matches, the resulting {@link TreeVisitResult} of the given visitor determines how the visitor proceeds.
   */
  forEachRecWhile<T>(root: any, type: Constructor<T>, visitor: DataObjectVisitor<T>) {
    dataObjectVisitors.forEachIfRecWhile(root, (type ? c => c instanceof type : c => true) as (candidate) => candidate is T, visitor);
  },

  /**
   * Visits all nodes and calls on each element matching the given type predicate the provided consumer.
   * If a node matches, child nodes of this node are not visited.
   */
  forEachIf<T>(root: any, typePredicate: (candidate) => candidate is T, consumer: (node: T) => void) {
    dataObjectVisitors.forEachIfRecWhile(root, typePredicate, node => {
      consumer(node);
      return TreeVisitResult.SKIP_SUBTREE;
    });
  },

  /**
   * Visits all nodes and calls on each element matching the given type predicate the provided consumer.
   * If a node matches, child nodes of this node are also visited.
   */
  forEachIfRec<T>(root: any, typePredicate: (candidate) => candidate is T, consumer: (node: T) => void) {
    dataObjectVisitors.forEachIfRecWhile(root, typePredicate, node => {
      consumer(node);
      return TreeVisitResult.CONTINUE;
    });
  },

  /**
   * Visits all nodes and calls on each element matching the given type predicate the provided visitor.
   * If a node matches, the resulting {@link TreeVisitResult} of the given visitor determines how the visitor proceeds.
   */
  forEachIfRecWhile<T>(root: any, typePredicate: (candidate) => candidate is T, visitor: DataObjectVisitor<T>) {
    if (!typePredicate) {
      typePredicate = (c => true) as (candidate) => candidate is T;
    }
    scout.create(DataObjectVisitHelper).visit(root, (node, context) => {
      if (typePredicate(node)) {
        return visitor(node, context);
      }
      return TreeVisitResult.CONTINUE;
    });
  }
};

/**
 * Helper class to visit data objects (depth first, pre-order).
 * <p>
 * {@link dataObjectVisitors} will be in most cases sufficient.
 */
export class DataObjectVisitHelper {

  /**
   * Visits the given node and all of its children using the visitor.
   */
  visit(node: any, visitor: DataObjectVisitor<any>) {
    this._visit(node, scout.create(DataObjectVisitorContext), visitor);
  }

  protected _visit(node: any, context: DataObjectVisitorContext, visitor: DataObjectVisitor<any>): TreeVisitResult {
    if (objects.isNullOrUndefined(node)) {
      return TreeVisitResult.CONTINUE;
    }

    if (node instanceof Set || objects.isArray(node)) {
      return this._visitNode(node, context, visitor, (n, c, v) => this._visitSetOrArray(n, c, v));
    }

    if (node instanceof Map) {
      return this._visitNode(node, context, visitor, (n, c, v) => this._visitMap(n, c, v));
    }

    if (node instanceof BaseDoEntity) {
      return this._visitNode(node, context, visitor, (n, c, v) => this._visitBaseDoEntity(n, c, v));
    }

    if (objects.isObject(node)) {
      return this._visitNode(node, context, visitor, (n, c, v) => this._visitObject(n, c, v));
    }

    return this._visitNode(node, context, visitor, (n, c, v) => this._visitAny(n, c, v));
  }

  /**
   * Visits the given node using the visitor.
   * If this results in {@link TreeVisitResult.TERMINATE} visiting is terminated.
   * If it does not result in {@link TreeVisitResult.SKIP_SUBTREE} the children of the given node are visited using the given chain.
   */
  protected _visitNode<T>(node: T, context: DataObjectVisitorContext, visitor: DataObjectVisitor<any>, chain: (n: T, c: DataObjectVisitorContext, v: DataObjectVisitor<any>) => TreeVisitResult): TreeVisitResult {
    if (context.containsParent(node)) {
      throw new Error('Unable to visit node. Reference cycle detected.');
    }

    const visitResult = visitor(node, context);
    if (visitResult === TreeVisitResult.TERMINATE) {
      return TreeVisitResult.TERMINATE;
    }

    if (visitResult !== TreeVisitResult.SKIP_SUBTREE) {
      context.pushParent(node);
      const chainVisitResult = chain(node, context, visitor);
      context.popParent();
      return chainVisitResult;
    }

    return TreeVisitResult.CONTINUE;
  }

  /**
   * Visits all elements of a {@link Set} or an {@link Array} using the visitor.
   */
  protected _visitSetOrArray(setOrArray: Set<any> | Array<any>, context: DataObjectVisitorContext, visitor: DataObjectVisitor<any>): TreeVisitResult {
    for (const element of setOrArray) {
      if (this._visit(element, context, visitor) === TreeVisitResult.TERMINATE) {
        return TreeVisitResult.TERMINATE;
      }
    }
    return TreeVisitResult.CONTINUE;
  }

  /**
   * Visits all keys and values of a {@link Map} using the visitor.
   */
  protected _visitMap(map: Map<any, any>, context: DataObjectVisitorContext, visitor: DataObjectVisitor<any>): TreeVisitResult {
    for (const [key, value] of map) {
      if (this._visit(key, context, visitor) === TreeVisitResult.TERMINATE) {
        return TreeVisitResult.TERMINATE;
      }
      if (this._visit(value, context, visitor) === TreeVisitResult.TERMINATE) {
        return TreeVisitResult.TERMINATE;
      }
    }
    return TreeVisitResult.CONTINUE;
  }

  /**
   * Visits all values and all contributions of a {@link BaseDoEntity} using the visitor.
   */
  protected _visitBaseDoEntity(baseDoEntity: BaseDoEntity, context: DataObjectVisitorContext, visitor: DataObjectVisitor<any>): TreeVisitResult {
    if (this._visitSetOrArray(
      Object
        .keys(baseDoEntity)
        .filter(key => !strings.startsWith(key, '_'))
        .map(key => baseDoEntity[key]),
      context,
      visitor
    ) === TreeVisitResult.TERMINATE) {
      return TreeVisitResult.TERMINATE;
    }
    return this._visitSetOrArray(baseDoEntity.getContributions(), context, visitor);
  }

  /**
   * Visits all values of an {@link object} using the visitor.
   */
  protected _visitObject(obj: object, context: DataObjectVisitorContext, visitor: DataObjectVisitor<any>): TreeVisitResult {
    return this._visitSetOrArray(Object.values(obj), context, visitor);
  }

  /**
   * Visits no child of the given element. Hook for subclasses.
   */
  protected _visitAny(node: any, context: DataObjectVisitorContext, visitor: DataObjectVisitor<any>): TreeVisitResult {
    return TreeVisitResult.CONTINUE;
  }
}

export type DataObjectVisitor<T> = (node: T, context: DataObjectVisitorContext) => TreeVisitResult;

export class DataObjectVisitorContext {

  protected _parents = [];

  get parents(): any[] {
    return [...this._parents];
  }

  parent(index = 0): any {
    return this._parents[this._parents.length - 1 - index];
  }

  containsParent(parent: any): boolean {
    return arrays.contains(this._parents, parent);
  }

  pushParent(parent: any) {
    this._parents.push(parent);
  }

  popParent(): any {
    return this._parents.pop();
  }
}
