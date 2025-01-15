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
import { BaseDoEntity, ObjectFactory, typeName } from '../../../eclipse-scout-core/src';
let Test2Do = class Test2Do extends BaseDoEntity {
    libDo; // reference to external DO in namespace 'ns' with alias
    lib2Do; // reference to external DO in namespace 'ns' without alias
};
__decorate([
    Reflect.metadata("scout.m.t", "ns.LibDo")
], Test2Do.prototype, "libDo", void 0);
__decorate([
    Reflect.metadata("scout.m.t", "ns.Lib2Do")
], Test2Do.prototype, "lib2Do", void 0);
Test2Do = __decorate([
    typeName('test.Test2')
], Test2Do);
export { Test2Do };
ObjectFactory.get().registerNamespace('test', {}); // declares namespace for current module
window["scout"]["DataObjectInventory"].get().add(Test2Do, "test.Test2", "test.Test2Do");
