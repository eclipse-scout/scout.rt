/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.scriptprocessor;

import org.junit.Assert;
import org.junit.Test;

public class ScriptProcessorTest implements ExampleScripts {

  @Test
  public void testMinifyJsWithYui() throws Exception {
    ScriptProcessor p = new ScriptProcessor();
    try {
      Assert.assertEquals(JS_MINIFY_OUTPUT, p.minifyJs(JS_INPUT));
    }
    finally {
      p.close();
    }
  }

  @Test
  public void testCompileCssWithLess() throws Exception {
    ScriptProcessor p = new ScriptProcessor();
    try {
      Assert.assertEquals(CSS_COMPILE_OUTPUT, p.compileCss(CSS_INPUT));
    }
    finally {
      p.close();
    }
  }

  @Test
  public void testMinifyCssWithYui() throws Exception {
    ScriptProcessor p = new ScriptProcessor();
    try {
      Assert.assertEquals(CSS_MINIFY_OUTPUT, p.minifyCss(CSS_INPUT));
    }
    finally {
      p.close();
    }
  }
}
