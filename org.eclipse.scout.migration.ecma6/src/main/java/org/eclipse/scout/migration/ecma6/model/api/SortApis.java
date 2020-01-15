/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6.model.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

public class SortApis implements Runnable{
  public static void main(String[] args) {
    new SortApis().run();
  }

  @Override
  public void run() {
    Path libraryApiDirectory = Configuration.get().getApiBase();
    if (libraryApiDirectory == null) {
      throw new ProcessingException("Library path must be defined!");
    }
    try {
      final Map<Path, NamedElement> libraries = readApi(libraryApiDirectory);
      for(Entry<Path, NamedElement> e : libraries.entrySet()){
        writeLibrary(e.getKey(), e.getValue());
      }
    }
    catch (IOException e) {
      throw new ProcessingException("Could not rewrite libraries.", e);
    }
  }



  protected Map<Path, NamedElement> readApi(Path libraryApiDirectory) throws IOException {
    final Map<Path, NamedElement> libraries = Files.list(libraryApiDirectory)
      .collect(Collectors.toMap(f -> f, ApiParser::parseLibrary));
    libraries.forEach((key, val) -> setParents(val, null));
    return libraries;
  }

  private void writeLibrary(Path fileName, NamedElement lib) throws IOException {
    ApiWriter.writeLibrary(fileName,lib);
  }

  private void setParents(INamedElement element, INamedElement parent) {
    element.setParent(parent);
    element.getChildren().forEach(child -> setParents(child, element));
  }
}
