/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
window.realXMLHttpRequest = window.XMLHttpRequest; // backup the real XMLHttpRequest object

/**
 * Map the stacktrace of the error given using the source map of the corresponding file.
 * @param {Error} error The error whose stacktrace should be mapped.
 * @returns {string} The mapped stacktrace as string.
 */
function mapStackTraceSync(error) {
  let mapped = '';
  window.sourceMappedStackTrace.mapStackTrace(error.stack, mappedStack => {
    mapped = mappedStack.join('\n');
  }, {sync: true, cacheGlobally: true});
  return mapped;
}

/**
 * Ensures that a real working XMLHttpRequest is available on the window object before mapping the stacktrace.
 * This is required because sourceMappedStackTrace performs ajax calls. And jasmine.Ajax might register a mock
 * for XMLHttpRequest which never returns source maps.
 * @param {Error} error The error to format.
 * @param {CallSite[]} structuredStackTrace more information about the stacktrace.
 * @returns {string} The formatted stacktrace as string.
 */
function mapStackTraceWithRealXmlHttpRequest(error, structuredStackTrace) {
  // noinspection UnnecessaryLocalVariableJS
  let originalXmlHttpReq = window.XMLHttpRequest;
  try {
    window.XMLHttpRequest = window.realXMLHttpRequest; // temporary uninstall jasmine.Ajax mock
    return mapStackTraceSync(error);
  } finally {
    window.XMLHttpRequest = originalXmlHttpReq; // restore original
  }
}

/**
 * V8 stacktrace API to customize the stacktrace of errors thrown.
 * @see https://v8.dev/docs/stack-trace-api
 */
try {
  Error.prepareStackTrace = mapStackTraceWithRealXmlHttpRequest;
} catch (e) {
  console.log('Unable to install source-mapped-stacktrace mapper.', e);
}
