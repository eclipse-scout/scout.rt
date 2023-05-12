import {JasmineScout} from '@eclipse-scout/core/testing';

import '../../main/js/index';

// @ts-expect-error
let context = require.context('./', true, /[sS]pec\.[tj]s$/);
JasmineScout.runTestSuite(context);
