/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, typeName} from '../../../eclipse-scout-core/src';
import {Test2Do} from './Test2Do'; // reference to class in own module

@typeName('test.Test')
export class TestDo extends BaseDoEntity {
  num: number;
  id: string;
  bool: boolean;
  arr1: string[];
  arr2: Array<Array<number>>;
  stringLiteralType: 'stringLiteral';
  secondInSameFileDo: SecondInSameFileDo<Array<boolean>>;
  set: Set<string>;
  map: Map<string, number>;
  noType;
  anyType: any;
  unknownType: unknown;
  voidType: void;
  ifcType: DoInterface;
  unionType: string | number | Set<string>;
  intersectionType: DoInterface & SecondInSameFileDo<any>;

  static ignoredBecauseStatic: string;
  protected ignoredBecauseProtected: string;
  _ignoredBecauseProtectedLike: string;
  $ignoredBecauseJQueryLike: string;
}

export const SECOND_TYPE_NAME = 'test.SecondInSameFile';

@typeName(SECOND_TYPE_NAME)
export class SecondInSameFileDo<T> extends BaseDoEntity {
  recordType: Record<string, Test2Do>;
}

export interface DoInterface {
}
