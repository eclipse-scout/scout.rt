/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
// Name this file 'test-index.ts' because it makes searching for 'index.ts' easier in a workspace with lots of different Scout web-projects.
import {JasmineScout} from '../src/testing/index';

// @ts-expect-error
let context = require.context('./', true, /[sS]pec\.[t|j]s$/);
JasmineScout.runTestSuite(context);
