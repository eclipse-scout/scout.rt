/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

// Copied from https://github.com/airbnb/ts-migrate (MIT License)
import ts from 'typescript';
import path from 'path';
const __dirname = path.resolve();

export function mockPluginParams(params) {
  const {
    fileName = 'file.ts',
    text = '',
    semanticDiagnostics = [],
    syntacticDiagnostics = [],
    suggestionDiagnostics = [],
    options = {}
  } = params;

  const sourceFile = ts.createSourceFile(
    fileName,
    text,
    ts.ScriptTarget.Latest,
    /* setParentNodes */ true
  );

  const withFile = diagnostic => ({
    ...diagnostic,
    file: sourceFile
  });

  return {
    options: options,
    fileName,
    rootDir: __dirname,
    text,
    sourceFile,
    getLanguageService: () =>
      ({
        getSemanticDiagnostics: () => semanticDiagnostics.map(withFile),
        getSyntacticDiagnostics: () => syntacticDiagnostics.map(withFile),
        getSuggestionDiagnostics: () => suggestionDiagnostics.map(withFile)
      })
  };
}

export function mockDiagnostic(
  text,
  errorText,
  overrides = {}
) {
  const index = text.indexOf(errorText);
  if (index === -1) {
    throw new Error(`Did not find ${errorText} in ${text}`);
  }

  return {
    messageText: 'diagnostic message',
    start: index,
    length: errorText.length,
    category: ts.DiagnosticCategory.Error,
    code: 123,
    ...overrides
  };
}
