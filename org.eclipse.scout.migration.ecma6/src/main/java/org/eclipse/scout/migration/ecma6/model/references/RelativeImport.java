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

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.scout.migration.ecma6.FileUtility;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;

/**
 *  used for imports in the same module like:<br>
 *  import FormField,
 */
public class RelativeImport extends AbstractImport<RelativeImport>{

  private String m_moduleName;

  public RelativeImport(String moduleName){
    m_moduleName = moduleName;
  }


  @Override
  public String getModuleName() {
    return m_moduleName;
  }

  public static String toKey(JsFile fileToImport){
    return fileToImport.getPath().toString();
  }


  public static String computeRelativePath(Path targetFile, Path modulePath){
    if(!Files.isDirectory(targetFile)){
      targetFile = targetFile.getParent();
    }
    Path relPath = FileUtility.removeFileExtensionJs(targetFile.relativize(modulePath));
    return relPath.toString();
  }
}
