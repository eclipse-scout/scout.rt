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
package org.eclipse.scout.migration.ecma6.model.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.exception.ProcessingException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ApiParser {

  private final ObjectMapper m_defaultJacksonObjectMapper;
  private Path m_directory;

  public ApiParser(Path directory){
    m_directory = directory;
    m_defaultJacksonObjectMapper = new ObjectMapper()
      .setSerializationInclusion(Include.NON_DEFAULT)
      .enable(SerializationFeature.INDENT_OUTPUT)
      .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
      .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
  }
  public Libraries parse() throws IOException {
    Libraries allLibs = new Libraries();
    List<INamedElement> libs = new ArrayList<>();
    Files.newDirectoryStream(m_directory).forEach(lib -> libs.add(parseLibrary(lib)));
    allLibs.addChildren(libs);
    allLibs.ensureParents();
    return allLibs;
  }

  protected NamedElement parseLibrary(Path lib) {
    try {
      NamedElement library = m_defaultJacksonObjectMapper.readValue(Files.newInputStream(lib), NamedElement.class);

      return library;

    }
    catch (IOException e) {
      throw new ProcessingException("Could parse Api of '"+lib+"'.",e);
    }
  }




}
