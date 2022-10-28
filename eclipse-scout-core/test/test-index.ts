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
// Name this file 'test-index.ts' because it makes searching for 'index.ts' easier in a workspace with lots of different Scout web-projects.
import {JasmineScout} from '../src/testing/index';

// @ts-ignore
let context = require.context('./', true, /[sS]pec\.[t|j]s$/);
JasmineScout.runTestSuite(context);
