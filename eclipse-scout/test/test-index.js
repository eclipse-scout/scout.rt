// Call this file 'test-index.js' because it makes searching for 'index.js'
// easier in a workspace with lots of different Scout web-projects.
let context = require.context('./', true, /[sS]pec\.js$/);
context.keys().forEach(context);
