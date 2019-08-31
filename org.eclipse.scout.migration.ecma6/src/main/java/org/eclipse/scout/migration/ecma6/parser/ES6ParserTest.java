/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6.parser;

import java.io.File;
import java.io.IOException;

import org.eclipse.scout.rt.platform.util.IOUtility;

public class ES6ParserTest {
  static int okPass1 = 0;
  static int okPass2 = 0;
  static int err = 0;

  public static void main(String[] args) throws IOException {
    //analyze("//use strict\nlet x=3;\nswitch(x){case 1: {\nbreak;\n}\ncase 2:{\nbreak;\n}\n}\n");

    //analyze(new File("C:\\dev\\workspaces\\bsicrm-16.2-j\\org.eclipse.scout.rt\\org.eclipse.scout.rt.ui.html\\src\\main\\js\\scout\\form\\Form.js"));

    parseWorkspaceRec(new File("C:\\dev\\workspaces\\bsicrm-16.2-j\\"));
    System.out.println("OK PASS1: " + okPass1 + ", OK PASS2: " + okPass2 + ", ERROR: " + err);
  }

  private static void analyze(File f) throws IOException {
    ES6Ast ast = new ES6Ast(f);
    ast.visitBreathFirst(System.out::println);
    IOUtility.writeContent(ast.getPath().replace(".js", "_gitignore.js"), ast.generateOutput());
  }

  private static void analyze(String code) throws IOException {
    ES6Ast ast = new ES6Ast("test.js", code);
    ast.visitBreathFirst(System.out::println);
  }

  private static void parseWorkspaceRec(File dir) throws IOException {
    if (dir.getName().startsWith(".")) return;
    if (dir.getName().equals("target")) return;
    if (dir.getName().equals("externalfiles")) return;
    if (dir.getName().equals("node_modules")) return;
    if (dir.getName().equals("WebContent")) return;
    if (dir.getName().equals("WebContent")) return;
    if (dir.getName().equals("org.eclipse.scout.docs")) return;
    if (dir.getName().equals("eclipse-scout-cli")) return;
    //if (dir.getName().equals("eclipse-scout")) return;
    //if (dir.getName().equals("eclipse-scout-core")) return;
    for (File f : dir.listFiles()) {
      if (f.isDirectory()) {
        parseWorkspaceRec(f);
        continue;
      }
      if (!f.getName().endsWith(".js")) {
        continue;
      }
      if (f.getName().startsWith("karma.conf")) continue;

      try {
        ES6Ast ast = new ES6Ast(f);
        System.out.println("OK PASS1 " + f);
        okPass1++;

        //re-parse generated output
        ast = new ES6Ast(ast.getPath(), ast.generateOutput());
        System.out.println("OK PASS2 " + f);
        okPass2++;
      }
      catch (Exception e) {
        System.out.println("ERROR " + f);
        err++;
      }
    }
  }
}
