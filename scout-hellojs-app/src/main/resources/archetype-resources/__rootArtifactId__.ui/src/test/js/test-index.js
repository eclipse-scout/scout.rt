import {JasmineScout} from '@eclipse-scout/core/testing';

import '../../main/js/index';

let context = require.context('./', true, /[sS]pec\.js$/);
JasmineScout.runTestSuite(context);
