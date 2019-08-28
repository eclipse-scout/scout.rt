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
package org.eclipse.scout.migration.ecma6.model.references;

public class LibraryImport  extends AbstractImport<LibraryImport>{

  private String m_moduleName;


  public LibraryImport(String moduleName){
    m_moduleName = moduleName;
  }


  public String getKey(){
    return m_moduleName;
  }

  @Override
  public String getModuleName()  {
    return m_moduleName;
  }


  public static String toKey(String moduleName){
    return moduleName;
  }
}
