/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.thirdparty;

import org.junit.Assert;
import org.junit.Test;

public class ThirdPartyScriptProcessorTest implements ExampleScripts {

  @Test
  public void testMinifyJsWithYui() throws Exception {
    ThirdPartyScriptProcessorImpl p = new ThirdPartyScriptProcessorImpl();
    Assert.assertEquals(JS_MINIFY_OUTPUT, p.minifyJs(JS_INPUT));
  }

  @Test
  public void testCompileCssWithLess() throws Exception {
    ThirdPartyScriptProcessorImpl p = new ThirdPartyScriptProcessorImpl();
    Assert.assertEquals(CSS_COMPILE_OUTPUT, p.compileCss(CSS_INPUT));
  }

  @Test
  public void testMinifyCssWithYui() throws Exception {
    ThirdPartyScriptProcessorImpl p = new ThirdPartyScriptProcessorImpl();
    Assert.assertEquals(CSS_MINIFY_OUTPUT, p.minifyCss(CSS_INPUT));
  }

}
