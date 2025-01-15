/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, ObjectFactory, typeName} from '../../../eclipse-scout-core/src';
import {Lib2Do, LibDo as OtherName} from 'lib';

@typeName('test.Test2')
export class Test2Do extends BaseDoEntity {
  libDo: OtherName; // reference to external DO in namespace 'ns' with alias
  lib2Do: Lib2Do; // reference to external DO in namespace 'ns' without alias
}

ObjectFactory.get().registerNamespace('test', {}); // declares namespace for current module
