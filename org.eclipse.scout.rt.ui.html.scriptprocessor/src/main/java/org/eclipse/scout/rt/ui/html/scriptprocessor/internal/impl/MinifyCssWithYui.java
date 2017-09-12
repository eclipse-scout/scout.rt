/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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

import com.yahoo.platform.yui.compressor.CssCompressor;

public class MinifyCssWithYui {

  public String run(String content) throws IOException {
    try (
        StringReader reader = new StringReader(content);
        StringWriter writer = new StringWriter()) {
      CssCompressor compressor = new CssCompressor(reader);
      compressor.compress(writer, -1);
      writer.flush();
      return writer.toString();
    }
  }

}
