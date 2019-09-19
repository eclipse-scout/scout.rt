/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
// Name this file 'test-index.js' because it makes searching for 'index.js' easier in a workspace with lots of different Scout web-projects.


let context = require.context('./', true, /Dummy.*[sS]pec\.js$/);
context.keys().forEach(context);
// TODO: remove lines above this comment and uncomment lines below as soon as the scout core code is available.
//import {JasmineScout} from '@eclipse-scout/testing';
//let context = require.context('./', true, /[sS]pec\.js$/);
//JasmineScout.runTestSuite(context);
