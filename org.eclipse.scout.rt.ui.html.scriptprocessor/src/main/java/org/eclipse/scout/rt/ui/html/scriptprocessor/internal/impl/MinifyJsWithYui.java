/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.scriptprocessor.internal.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class MinifyJsWithYui {

  public String run(String content) throws IOException {
    return run(content, false);
  }

  public String run(String content, boolean munge) throws IOException {
    try (
        StringReader reader = new StringReader(content);
        StringWriter writer = new StringWriter()) {

      // Yui uses a modified version of rhino files (org.mozilla.javascript). If you get any class incompatibility errors this may be the reason.
      ErrorReporter errorReporter = new ErrorReporter() {

        @Override
        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
          System.out.println(MinifyJsWithYui.class.getSimpleName() + " warning: " + message);
        }

        @Override
        public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
          System.out.println(MinifyJsWithYui.class.getSimpleName() + " error: " + message);
          return null;
        }

        @Override
        public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
          System.out.println(MinifyJsWithYui.class.getSimpleName() + " error: " + message);
        }
      };
      JavaScriptCompressor compressor = new JavaScriptCompressor(reader, errorReporter);
      boolean verbose = false;
      boolean preserveAllSemicolons = false;
      boolean disableOptimizations = false;
      compressor.compress(writer, -1, munge, verbose, preserveAllSemicolons, disableOptimizations);
      writer.flush();
      return writer.toString();
    }
  }

}
