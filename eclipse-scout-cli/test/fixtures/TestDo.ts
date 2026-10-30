/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/* eslint-disable */

import {BaseDoEntity, typeName} from '../../../eclipse-scout-core/src';
import {ImportedDoAlias, ImportedLiteralUnion, ImportedStringEnum, Test2Do} from './Test2Do'; // reference to class in own module
import {BuiltColor, BuiltDoAlias, BuiltEnum, BuiltIdTypeName, BuiltPermissionLevel} from 'libbuilt'; // reference to a built module (declaration files only)
import {ImportedStringEnum as RenamedEnum, Test2Do as RenamedTest2Do} from './Test2Do';
import {LibDo as RenamedLibDo} from 'lib';

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
  initNum = 10;
  initNegativeNum = -1.5;
  initString = 'abc';
  initTemplate = `abc`;
  initBool = false;
  initSet = new Set<string>();
  initMap = new Map<string, Array<number>>();
  initDate = new Date();
  initDo = new Test2Do();
  initGenericDo = new SecondInSameFileDo<boolean>();
  initArr = [1, 2];
  initMixedArr = [1, 'a'];
  initEmptyArr = [];
  initAs = [] as string[];
  initTypePrecedence: string = null;
  initNull = null;

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

@typeName(ThirdInSameFileDo.TYPE_NAME)
export class ThirdInSameFileDo extends BaseDoEntity {
  num: number;
  static TYPE_NAME = 'test.ThirdInSameFile';
}

@typeName('test.WithGenericDoProperty')
export class WithGenericDoPropertyDo<T extends ThirdInSameFileDo> extends BaseDoEntity {
  items: T[];
}

export interface DoInterface {
}

enum StringEnum {
  A = 'a',
  B = 'b'
}

enum NumericEnum {
  A,
  B = 5,
  C = -1
}

enum MixedEnum {
  A = 1,
  B = 'b'
}

type LiteralUnion = 'a' | 'b';
type NumberAlias = number;
type DoAlias = ThirdInSameFileDo;
type DoArrayAlias = ThirdInSameFileDo[];
type GenericAlias<X> = X[];
type RecursiveAlias = RecursiveAlias[];
type MixedUnion = string | number;
const ID_TYPE_NAME = 'test.IdType';
type IdTypeName = typeof ID_TYPE_NAME;
const Colors = {RED: 'red', BLUE: 'blue'} as const;
type EnumObjectLike<X> = X[keyof X];
type Color = EnumObjectLike<typeof Colors>;

class Permission {
  static Level = {
    NONE: 0,
    ALL: 100
  };
}

type PermissionLevel = EnumObjectLike<typeof Permission.Level>;

interface Comparable<X> {
}

@typeName('test.TypeResolution')
export class TypeResolutionDo<T, D = Date, C extends Comparable<C> = null> extends BaseDoEntity {
  unconstrainedTypeParam: T;
  defaultTypeParam: D;
  recursiveConstraint: C;
  stringEnum: StringEnum;
  numericEnum: NumericEnum;
  mixedEnum: MixedEnum;
  importedEnum: ImportedStringEnum;
  literalUnion: LiteralUnion;
  importedLiteralUnion: ImportedLiteralUnion;
  numberAlias: NumberAlias;
  doAlias: DoAlias;
  importedDoAlias: ImportedDoAlias;
  doArrayAlias: DoArrayAlias;
  genericAlias: GenericAlias<string>;
  recursiveAlias: RecursiveAlias;
  mixedUnion: MixedUnion;
  nullable: Date | null;
  optional: ThirdInSameFileDo | undefined;
  literalNumber: 1;
  literalBoolean: true;
  readonlyArray: readonly string[];
  readonlyArrayType: ReadonlyArray<number>;
  readonlySet: ReadonlySet<string>;
  partial: Partial<ThirdInSameFileDo>;
  parenthesized: (string)[];
  literalArray: LiteralUnion[];
  literalSet: Set<'a' | 'b'>;
  promise: Promise<string>;
  globalFile: File;
  idLike: SecondInSameFileDo<'scout.SimpleUuid'>;
  idLikeUnion: SecondInSameFileDo<'a' | 'b'>;
  idFromConstant: SecondInSameFileDo<IdTypeName>;
  typeofConstant: IdTypeName;
  enumObject: Color;
  staticEnumObject: PermissionLevel;
  builtEnum: BuiltEnum;
  builtEnumObject: BuiltColor;
  builtStaticEnumObject: BuiltPermissionLevel;
  builtIdFromConstant: SecondInSameFileDo<BuiltIdTypeName>;
  builtDoAlias: BuiltDoAlias;
  renamedDo: RenamedTest2Do;
  renamedEnum: RenamedEnum;
  renamedExternalDo: RenamedLibDo;
}
