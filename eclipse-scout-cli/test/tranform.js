/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
const ts = require('typescript');
const DataObjectTransformer = require('../scripts/DataObjectTransformer');
const ModuleNamespaceResolver = require('../scripts/ModuleNamespaceResolver');

function compile(fileNames, options) {
  const program = ts.createProgram(fileNames, options);
  const namespaceResolver = new ModuleNamespaceResolver();
  const emitResult = program.emit(undefined, undefined, undefined, undefined, {
    before: [ctx => {
      const doTransformer = new DataObjectTransformer(program, ctx, namespaceResolver);
      return node => ts.visitNode(node, node => doTransformer.transform(node));
    }]
  });

  const allDiagnostics = ts.getPreEmitDiagnostics(program).concat(emitResult.diagnostics);
  allDiagnostics.forEach(diagnostic => {
    if (diagnostic.file) {
      const {line, character} = ts.getLineAndCharacterOfPosition(diagnostic.file, diagnostic.start);
      const message = ts.flattenDiagnosticMessageText(diagnostic.messageText, '\n');
      console.log(`${diagnostic.file.fileName} (${line + 1},${character + 1}): ${message}`);
    } else {
      console.log(ts.flattenDiagnosticMessageText(diagnostic.messageText, '\n'));
    }
  });

  return emitResult.emitSkipped ? 1 : 0;
}

// console.log(`Process exiting with code '${exitCode}'.`);
// process.exit(exitCode);

const compilerOptions = {
  target: ts.ScriptTarget.ESNext,
  moduleResolution: ts.ModuleResolutionKind.Bundler,
  module: ts.ModuleKind.ESNext,
  skipLibCheck: true,
  alwaysStrict: true,
  strictBindCallApply: true,
  noImplicitOverride: true,
  useDefineForClassFields: true,
  allowSyntheticDefaultImports: true,
  experimentalDecorators: true
};

module.exports = files => {
  return compile(files, compilerOptions);
};


