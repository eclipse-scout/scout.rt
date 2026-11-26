/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * AI Disclosure: This file was partially AI-generated.
 * The AI-generated portions are made available under CC0-1.0
 * and not subject to the project's licence.
 *
 * SPDX-License-Identifier: EPL-2.0 and CC0-1.0
 */
const ts = require('typescript');
const path = require('path');
const fs = require('fs');
const DataObjectTransformer = require('../scripts/data-object/DataObjectTransformer');
const ModuleResolver = require('../scripts/data-object/ModuleResolver');

function compile(fileNames, options) {
  const program = ts.createProgram(fileNames, options);
  const moduleResolver = new ModuleResolver();
  const transformers = {
    before: [ctx => {
      const doTransformer = new DataObjectTransformer(program, ctx, moduleResolver);
      return node => ts.visitNode(node, node => doTransformer.transform(node));
    }]
  };

  // only emit the fixtures, not the referenced Scout sources
  const fixturesDir = path.resolve(__dirname, 'fixtures') + path.sep;
  const emitResults = program.getSourceFiles()
    .filter(sourceFile => !sourceFile.isDeclarationFile && path.resolve(sourceFile.fileName).startsWith(fixturesDir))
    .map(sourceFile => program.emit(sourceFile, undefined, undefined, undefined, transformers));
  const emitResult = {
    emitSkipped: emitResults.some(result => result.emitSkipped),
    diagnostics: emitResults.flatMap(result => result.diagnostics)
  };

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

function readCompilerOptions() {
  const configFileName = path.resolve(__dirname, '../tsconfig.json');
  const overrides = {noEmit: false, sourceMap: false, allowJs: false};
  const parsed = ts.getParsedCommandLineOfConfigFile(configFileName, overrides, {
    ...ts.sys,
    onUnRecoverableConfigFileDiagnostic: diagnostic => {
      throw new Error(ts.flattenDiagnosticMessageText(diagnostic.messageText, '\n'));
    }
  });
  return parsed.options;
}

const compilerOptions = readCompilerOptions();

/**
 * Transpiles a single file without type information like ts-loader does if types are not checked (transpileOnly).
 * @returns {string} The transpiled JavaScript
 */
function transpile(fileName, options) {
  const moduleResolver = new ModuleResolver();
  return ts.transpileModule(fs.readFileSync(fileName, 'utf-8'), {
    fileName: path.resolve(fileName),
    compilerOptions: options,
    transformers: {
      before: [ctx => {
        const doTransformer = new DataObjectTransformer(undefined, ctx, moduleResolver);
        return node => ts.visitNode(node, node => doTransformer.transform(node));
      }]
    }
  }).outputText;
}

module.exports = {
  compile: fileNames => compile(fileNames, compilerOptions),
  transpile: fileName => transpile(fileName, compilerOptions)
};
