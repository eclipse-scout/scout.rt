var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import { BaseDoEntity, typeName } from '../../../eclipse-scout-core/src';
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
    Reflect.metadata("scout.m.t", "stringLiteral")
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
    Reflect.metadata("scout.m.t", Object)
], TestDo.prototype, "anyType", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Object)
], TestDo.prototype, "unknownType", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Object)
], TestDo.prototype, "voidType", void 0);
__decorate([
    Reflect.metadata("scout.m.t", "test.DoInterface")
], TestDo.prototype, "ifcType", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Object)
], TestDo.prototype, "unionType", void 0);
__decorate([
    Reflect.metadata("scout.m.t", Object)
], TestDo.prototype, "intersectionType", void 0);
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
window["scout"]["DataObjectInventory"].get().add(TestDo, "test.Test", "test.TestDo");
window["scout"]["DataObjectInventory"].get().add(SecondInSameFileDo, "test.SecondInSameFile", "test.SecondInSameFileDo");
