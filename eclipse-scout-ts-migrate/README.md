This module can do a base migration of Scout JavaScript to TypeScript code.
It uses the tool [ts-migrate](https://github.com/airbnb/ts-migrate) created by airbnb.
This tool has a plugin architecture where each plugin handles a transformation.

Some plugins use typescript to transform the code, which works but does not preserve empty lines and
deliberate line breaks.
Other plugins use codeshift which tries to preserve to original source code style as much as possible.

[Codeshift](https://github.com/facebook/jscodeshift) is a wrapper around [Recast](https://github.com/benjamn/recast) and [AST-Types](https://github.com/benjamn/ast-types).
It mainly simplifies visiting the AST tree. It also supports TypeScript code.

The API for AST can be found at https://github.com/benjamn/ast-types/tree/master/def, especially at [core.js](https://github.com/benjamn/ast-types/blob/master/def/core.ts).
For example, the find method takes a type as parameter. Some types "inherit" from other types.
The API shows the available types and theirs relationships, e.g. the base class is defined using .bases("xy).

Even though this "documentation" can help a bit, what really helps is to analyze the AST tree of existing code using https://astexplorer.net/ or https://ts-ast-viewer.com/# for TypeScript.

A good overview about AST with JavaScript gives this article: https://www.digitalocean.com/community/tutorials/js-traversing-ast.

Examples of codeshift transformations can be found here: https://github.com/cpojer/js-codemod/

Finally and most importantly: visit https://www.codeshiftcommunity.com/ for documentation and examples.

Because the plugin `memberAccessibilityPlugin` breaks empty lines and various new line, it was replaced
by a custom implementation.

The plugin `declareMissingClassProperties` also has some limitations. It was copied and adjusted to our needs.
- Only consider members defined in the constructor because others have most likely already been defined in the super class.
- Add types based on value and naming convention.

An alternative to the crlf plugins should be to pass the lineTerminator to recast `.toSource({lineTerminator: '\n'});`.
But it somehow does not work and would need more investigation...
