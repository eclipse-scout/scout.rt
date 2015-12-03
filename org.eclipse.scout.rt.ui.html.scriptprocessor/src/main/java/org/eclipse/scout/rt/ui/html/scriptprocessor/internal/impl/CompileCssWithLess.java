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

import org.eclipse.scout.rt.ui.html.scriptprocessor.internal.loader.ScoutClasspathResourceLoader;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;
import com.asual.lesscss.LessOptions;
import com.asual.lesscss.loader.ResourceLoader;

public class CompileCssWithLess {

  public String run(String content) throws IOException {
    LessOptions options = new LessOptions();
    ResourceLoader resourceLoader = new ScoutClasspathResourceLoader(LessEngine.class.getClassLoader());
    LessEngine engine = new LessEngine(options, resourceLoader);
    try {
      return engine.compile(content);
    }
    catch (LessException e) {
      throw new IOException("Failed to parse CSS content with LESS", e);
    }
  }

}
