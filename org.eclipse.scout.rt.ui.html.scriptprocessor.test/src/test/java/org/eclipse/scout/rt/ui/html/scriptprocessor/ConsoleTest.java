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
package org.eclipse.scout.rt.ui.html.scriptprocessor;

public final class ConsoleTest implements ExampleScripts {

  private ConsoleTest() {
  }

  public static void main(String[] args) throws Exception {
    ScriptProcessor impl = new ScriptProcessor();
    try {
      System.out.println("minifyJs: " + impl.minifyJs(JS_INPUT));
      System.out.println("compileCss: " + impl.compileCss(CSS_INPUT));
      System.out.println("minifyCss: " + impl.minifyCss(CSS_INPUT));
    }
    finally {
      impl.close();
    }
  }
}
