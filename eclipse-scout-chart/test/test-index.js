/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {JasmineScout} from '@eclipse-scout/testing';

let context = require.context('./', true, /[sS]pec\.js$/);
JasmineScout.runTestSuite(context);
