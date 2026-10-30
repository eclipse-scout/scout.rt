var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
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
import { BaseDoEntity, typeName } from '../../../eclipse-scout-core/src';
import { Test2Do } from './Test2Do'; // reference to class in own module
let TestDo = class TestDo extends BaseDoEntity {
    num;
    id;
    bool;
    arr1;
    arr2;
    stringLiteralType;
    secondInSameFileDo;
    set;
    map;
    noType;
    anyType;
    unknownType;
    voidType;
    ifcType;
    unionType;
    intersectionType;
    initNum = 10;
    initNegativeNum = -1.5;
    initString = 'abc';
    initTemplate = `abc`;
    initBool = false;
    initSet = new Set();
    initMap = new Map();
    initDate = new Date();
    initDo = new Test2Do();
    initGenericDo = new SecondInSameFileDo();
    initArr = [1, 2];
    initMixedArr = [1, 'a'];
    initEmptyArr = [];
    initAs = [];
    initTypePrecedence = null;
    initNull = null;
    static ignoredBecauseStatic;
    ignoredBecauseProtected;
    _ignoredBecauseProtectedLike;
    $ignoredBecauseJQueryLike;
};
__decorate([
    Reflect.metadata("scout.m.t", Number)
], TestDo.prototype, "num", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TestDo.prototype, "id", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Boolean)
], TestDo.prototype, "bool", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Array, typeArgs: [String] })
], TestDo.prototype, "arr1", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Array, typeArgs: [{ objectType: Array, typeArgs: [Number] }] })
], TestDo.prototype, "arr2", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TestDo.prototype, "stringLiteralType", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: "test.SecondInSameFileDo", typeArgs: [{ objectType: Array, typeArgs: [Boolean] }] })
], TestDo.prototype, "secondInSameFileDo", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Set, typeArgs: [String] })
], TestDo.prototype, "set", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Map, typeArgs: [String, Number] })
], TestDo.prototype, "map", void 0);
__decorate([
    Reflect.metadata("scout.m.t", "test.DoInterface")
], TestDo.prototype, "ifcType", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Number)
], TestDo.prototype, "initNum", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Number)
], TestDo.prototype, "initNegativeNum", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TestDo.prototype, "initString", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TestDo.prototype, "initTemplate", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Boolean)
], TestDo.prototype, "initBool", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Set, typeArgs: [String] })
], TestDo.prototype, "initSet", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Map, typeArgs: [String, { objectType: Array, typeArgs: [Number] }] })
], TestDo.prototype, "initMap", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Date)
], TestDo.prototype, "initDate", void 0);
__decorate([
    Reflect.metadata("scout.m.t", "test.Test2Do")
], TestDo.prototype, "initDo", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: "test.SecondInSameFileDo", typeArgs: [Boolean] })
], TestDo.prototype, "initGenericDo", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Array, typeArgs: [Number] })
], TestDo.prototype, "initArr", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Array, typeArgs: [null] })
], TestDo.prototype, "initMixedArr", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Array, typeArgs: [null] })
], TestDo.prototype, "initEmptyArr", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Array, typeArgs: [String] })
], TestDo.prototype, "initAs", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TestDo.prototype, "initTypePrecedence", void 0);
TestDo = __decorate([
    typeName('test.Test')
], TestDo);
export { TestDo };
export const SECOND_TYPE_NAME = 'test.SecondInSameFile';
let SecondInSameFileDo = class SecondInSameFileDo extends BaseDoEntity {
    recordType;
};
__decorate([
    Reflect.metadata("scout.m.t", { objectType: "Record", typeArgs: [String, "test.Test2Do"] })
], SecondInSameFileDo.prototype, "recordType", void 0);
SecondInSameFileDo = __decorate([
    typeName(SECOND_TYPE_NAME)
], SecondInSameFileDo);
export { SecondInSameFileDo };
let ThirdInSameFileDo = class ThirdInSameFileDo extends BaseDoEntity {
    num;
    static TYPE_NAME = 'test.ThirdInSameFile';
};
__decorate([
    Reflect.metadata("scout.m.t", Number)
], ThirdInSameFileDo.prototype, "num", void 0);
ThirdInSameFileDo = __decorate([
    typeName(ThirdInSameFileDo.TYPE_NAME)
], ThirdInSameFileDo);
export { ThirdInSameFileDo };
let WithGenericDoPropertyDo = class WithGenericDoPropertyDo extends BaseDoEntity {
    items;
};
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Array, typeArgs: ["test.ThirdInSameFileDo"] })
], WithGenericDoPropertyDo.prototype, "items", void 0);
WithGenericDoPropertyDo = __decorate([
    typeName('test.WithGenericDoProperty')
], WithGenericDoPropertyDo);
export { WithGenericDoPropertyDo };
var StringEnum;
(function (StringEnum) {
    StringEnum["A"] = "a";
    StringEnum["B"] = "b";
})(StringEnum || (StringEnum = {}));
var NumericEnum;
(function (NumericEnum) {
    NumericEnum[NumericEnum["A"] = 0] = "A";
    NumericEnum[NumericEnum["B"] = 5] = "B";
    NumericEnum[NumericEnum["C"] = -1] = "C";
})(NumericEnum || (NumericEnum = {}));
var MixedEnum;
(function (MixedEnum) {
    MixedEnum[MixedEnum["A"] = 1] = "A";
    MixedEnum["B"] = "b";
})(MixedEnum || (MixedEnum = {}));
const ID_TYPE_NAME = 'test.IdType';
const Colors = { RED: 'red', BLUE: 'blue' };
class Permission {
    static Level = {
        NONE: 0,
        ALL: 100
    };
}
let TypeResolutionDo = class TypeResolutionDo extends BaseDoEntity {
    unconstrainedTypeParam;
    defaultTypeParam;
    recursiveConstraint;
    stringEnum;
    numericEnum;
    mixedEnum;
    importedEnum;
    literalUnion;
    importedLiteralUnion;
    numberAlias;
    doAlias;
    importedDoAlias;
    doArrayAlias;
    genericAlias;
    recursiveAlias;
    mixedUnion;
    nullable;
    optional;
    literalNumber;
    literalBoolean;
    readonlyArray;
    readonlyArrayType;
    readonlySet;
    partial;
    parenthesized;
    literalArray;
    literalSet;
    promise;
    globalFile;
    idLike;
    idLikeUnion;
    idFromConstant;
    typeofConstant;
    enumObject;
    staticEnumObject;
    builtEnum;
    builtEnumObject;
    builtStaticEnumObject;
    builtIdFromConstant;
    builtDoAlias;
    renamedDo;
    renamedEnum;
    renamedExternalDo;
};
__decorate([
    Reflect.metadata("scout.m.t", Date)
], TypeResolutionDo.prototype, "defaultTypeParam", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: "test.Comparable", typeArgs: [null] })
], TypeResolutionDo.prototype, "recursiveConstraint", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TypeResolutionDo.prototype, "stringEnum", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Number)
], TypeResolutionDo.prototype, "numericEnum", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TypeResolutionDo.prototype, "importedEnum", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TypeResolutionDo.prototype, "literalUnion", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TypeResolutionDo.prototype, "importedLiteralUnion", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Number)
], TypeResolutionDo.prototype, "numberAlias", void 0);
__decorate([
    Reflect.metadata("scout.m.t", "test.ThirdInSameFileDo")
], TypeResolutionDo.prototype, "doAlias", void 0);
__decorate([
    Reflect.metadata("scout.m.t", "test.Test2Do")
], TypeResolutionDo.prototype, "importedDoAlias", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Array, typeArgs: ["test.ThirdInSameFileDo"] })
], TypeResolutionDo.prototype, "doArrayAlias", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Array, typeArgs: [null] })
], TypeResolutionDo.prototype, "recursiveAlias", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Date)
], TypeResolutionDo.prototype, "nullable", void 0);
__decorate([
    Reflect.metadata("scout.m.t", "test.ThirdInSameFileDo")
], TypeResolutionDo.prototype, "optional", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Number)
], TypeResolutionDo.prototype, "literalNumber", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Boolean)
], TypeResolutionDo.prototype, "literalBoolean", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Array, typeArgs: [String] })
], TypeResolutionDo.prototype, "readonlyArray", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Array, typeArgs: [Number] })
], TypeResolutionDo.prototype, "readonlyArrayType", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Set, typeArgs: [String] })
], TypeResolutionDo.prototype, "readonlySet", void 0);
__decorate([
    Reflect.metadata("scout.m.t", "test.ThirdInSameFileDo")
], TypeResolutionDo.prototype, "partial", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Array, typeArgs: [String] })
], TypeResolutionDo.prototype, "parenthesized", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Array, typeArgs: [String] })
], TypeResolutionDo.prototype, "literalArray", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: Set, typeArgs: [String] })
], TypeResolutionDo.prototype, "literalSet", void 0);
__decorate([
    Reflect.metadata("scout.m.t", File)
], TypeResolutionDo.prototype, "globalFile", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: "test.SecondInSameFileDo", typeArgs: [String] })
], TypeResolutionDo.prototype, "idLike", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: "test.SecondInSameFileDo", typeArgs: [String] })
], TypeResolutionDo.prototype, "idLikeUnion", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: "test.SecondInSameFileDo", typeArgs: [String] })
], TypeResolutionDo.prototype, "idFromConstant", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TypeResolutionDo.prototype, "typeofConstant", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TypeResolutionDo.prototype, "enumObject", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Number)
], TypeResolutionDo.prototype, "staticEnumObject", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TypeResolutionDo.prototype, "builtEnum", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TypeResolutionDo.prototype, "builtEnumObject", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Number)
], TypeResolutionDo.prototype, "builtStaticEnumObject", void 0);
__decorate([
    Reflect.metadata("scout.m.t", { objectType: "test.SecondInSameFileDo", typeArgs: [String] })
], TypeResolutionDo.prototype, "builtIdFromConstant", void 0);
__decorate([
    Reflect.metadata("scout.m.t", "ns.LibDo")
], TypeResolutionDo.prototype, "builtDoAlias", void 0);
__decorate([
    Reflect.metadata("scout.m.t", "test.Test2Do")
], TypeResolutionDo.prototype, "renamedDo", void 0);
__decorate([
    Reflect.metadata("scout.m.t", String)
], TypeResolutionDo.prototype, "renamedEnum", void 0);
__decorate([
    Reflect.metadata("scout.m.t", "ns.LibDo")
], TypeResolutionDo.prototype, "renamedExternalDo", void 0);
TypeResolutionDo = __decorate([
    typeName('test.TypeResolution')
], TypeResolutionDo);
export { TypeResolutionDo };
window["scout"]["DataObjectInventory"].get().add(TestDo, "test.Test", "test.TestDo");
window["scout"]["DataObjectInventory"].get().add(SecondInSameFileDo, "test.SecondInSameFile", "test.SecondInSameFileDo");
window["scout"]["DataObjectInventory"].get().add(ThirdInSameFileDo, "test.ThirdInSameFile", "test.ThirdInSameFileDo");
window["scout"]["DataObjectInventory"].get().add(WithGenericDoPropertyDo, "test.WithGenericDoProperty", "test.WithGenericDoPropertyDo");
window["scout"]["DataObjectInventory"].get().add(TypeResolutionDo, "test.TypeResolution", "test.TypeResolutionDo");
