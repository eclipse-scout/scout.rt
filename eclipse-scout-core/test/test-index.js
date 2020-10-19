/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
// Name this file 'test-index.js' because it makes searching for 'index.js' easier in a workspace with lots of different Scout web-projects.
import {JasmineScout} from '../src/testing/index';

let context = require.context('./', true, /[sS]pec\.js$/);
JasmineScout.runTestSuite(context);
