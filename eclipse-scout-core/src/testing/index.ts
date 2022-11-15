/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
