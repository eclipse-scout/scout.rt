/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
// /////////////////////////////////////////////////////////////////
// TEST SUPPORT - DO NOT USE IN PRODUCTION CODE
// /////////////////////////////////////////////////////////////////
import {ObjectFactory} from '../index';
import * as self from './index';

export * from './TestingApp';
export * from './JasmineScout';
export * from './JasmineScoutUtil';
export * from './text/LocaleSpecHelper';
export * from './menu/MenuSpecHelper';
export * from './tree/TreeSpecHelper';
export * from './table/TableSpecHelper';
export * from './form/FormSpecHelper';
export * from './form/fields/CloneSpecHelper';
export * from './form/fields/beanfield/TestBeanField';
export * from './form/fields/tabbox/TabBoxSpecHelper';
export * from './desktop/outline/OutlineSpecHelper';
export * from './lookup/DummyLookupCall';
export * from './lookup/AbortableMicrotaskStaticLookupCall';
export * from './lookup/ActiveDummyLookupCall';
export * from './lookup/ColumnDescriptorDummyLookupCall';
export * from './lookup/LanguageDummyLookupCall';
export * from './focus/FocusManagerSpecHelper';
export * from './form/fields/groupbox/GroupBoxSpecHelper';
export * from './jquery-testing';
export * from './form/SpecForm';
export * from './form/SpecLifecycle';
export * from './form/SpecRadioButtonGroup';
export * from './jqueryExtensions-types';
export * from './scoutMatchers';
export * from './table/SpecTable';
export * from './table/SpecTableAdapter';
export * from './tree/SpecTree';
export * from './tree/SpecTreeAdapter';

export default self;
ObjectFactory.get().registerNamespace('scout', self);
