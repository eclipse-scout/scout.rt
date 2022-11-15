import {JasmineScout} from '@eclipse-scout/core/testing';

import * as ref1 from '../../main/js/index';

Object.assign({}, ref1);

let context = require.context('./', true, /[sS]pec\.js$/);
JasmineScout.runTestSuite(context);
