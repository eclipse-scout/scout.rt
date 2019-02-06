http://log4javascript.org/

Shipped version: 1.4.9 (2014-05-12)
https://sourceforge.net/projects/log4javascript/files/log4javascript/1.4.9/

ORIGINAL FILE                      TARGET FILE
---------------------------------------------------------------------------------------------------
log4javascript_uncompressed.js     src/main/resources/WebContent/res/log4javascript-1.4.9/log4javascript.js
log4javascript.js                  src/main/resources/WebContent/res/log4javascript-1.4.9/log4javascript.min.js
console_uncompressed.html          src/main/resources/WebContent/res/log4javascript-1.4.9/console_uncompressed.html
console.html                       src/main/resources/WebContent/res/log4javascript-1.4.9/console.html

Because the default CSP rules forbid inline javascript code, the original code has been modified:

The script tags in console_uncompressed.html have been removed and replaced by:
<script type="text/javascript" src="console_uncompressed.js"></script>

"console_uncompressed.js" contains the inline javascript code. The code to detect Internet
Explorer has been modified to use the scout.device utility instead. Similarly, console.html
has been modified with the minified code.

logging.js sets the property "useDocumentWrite" to false when initializing lov4javascript.
