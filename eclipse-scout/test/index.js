let context = require.context('./', true, /[sS]pec\.js$/);
context.keys().forEach(context);
