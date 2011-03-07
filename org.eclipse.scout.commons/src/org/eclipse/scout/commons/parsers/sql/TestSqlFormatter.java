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
package org.eclipse.scout.commons.parsers.sql;

import java.io.FileReader;

import org.eclipse.scout.commons.IOUtility;

public class TestSqlFormatter {

  public static void main(String[] args) throws Exception {
    String s = IOUtility.getContent(new FileReader("D:/TEMP/imo/a.sql"));
    //
    String w = SqlFormatter.wellform(s);
    System.out.println(w);
    System.exit(0);
  }
}
