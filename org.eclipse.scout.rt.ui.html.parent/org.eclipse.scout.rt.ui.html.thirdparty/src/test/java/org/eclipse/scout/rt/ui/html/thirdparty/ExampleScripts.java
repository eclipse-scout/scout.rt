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

/**
 *
 */
public interface ExampleScripts {
  String JS_INPUT = "" +
      "var abc=function(){\n" +
      "  window.x=1;\n" +
      "  alert('hello');\n" +
      "};\n" +
      "abc();\n";

  String JS_MINIFY_OUTPUT = "var abc=function(){window.x=1;alert(\"hello\")};abc();";

  String CSS_INPUT = "" +
      "@size-desktop-navigation: 290px;\n" +
      "@color-nav: #000;\n" +
      "@color-bench: #fff;\n" +
      ".desktop {\n" +
      "  position: relative;\n" +
      "  height: 100%;\n" +
      "    width: @size-desktop-navigation;\n" +
      "    background-color: @color-nav;\n" +
      "    color: @color-bench;\n" +
      "}\n";

  String CSS_COMPILE_OUTPUT = "" +
      ".desktop {\n" +
      "  position: relative;\n" +
      "  height: 100%;\n" +
      "  width: 290px;\n" +
      "  background-color: #000000;\n" +
      "  color: #ffffff;\n" +
      "}\n";

  String CSS_MINIFY_OUTPUT = "@size-desktop-navigation:290px;@color-nav:#000;@color-bench:#fff;.desktop{position:relative;height:100%;width:@size-desktop-navigation;background-color:@color-nav;color:@color-bench}";

}
