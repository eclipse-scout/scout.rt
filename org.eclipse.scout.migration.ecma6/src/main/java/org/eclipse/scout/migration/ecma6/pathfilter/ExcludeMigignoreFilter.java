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
package org.eclipse.scout.migration.ecma6.pathfilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcludeMigignoreFilter implements IMigrationExcludePathFilter {
  private static final Logger LOG = LoggerFactory.getLogger(ExcludeMigignoreFilter.class);

  private Set<Path> m_ignoredFiles = new HashSet<>();

  @PostConstruct
  public void setup() {
    Path migignoreFile = Configuration.get().getSourceModuleDirectory().resolve(".migignore");
    if (Files.exists(migignoreFile)) {
      try {
        m_ignoredFiles = readMigignore(migignoreFile);
      }
      catch (IOException e) {
        throw new ProcessingException("Could not parse migignore file: " + migignoreFile);
      }
    }
  }

  private Set<Path> readMigignore(Path migignoreFile) throws IOException {
    //noinspection resource
    return Files.lines(migignoreFile).map(line -> Paths.get(line)).collect(Collectors.toSet());
  }

  @Override
  public boolean test(PathInfo pathInfo) {
    if (m_ignoredFiles.contains(pathInfo.getModuleRelativePath())) {
      LOG.debug("Migignore excluded file: " + pathInfo);
      return true;
    }
    return false;
  }
}
