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
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.configuration.MigrationConfigProperties.IncludeFilesProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

public class IncludeVmArgPathFilter implements IMigrationIncludePathFilter {
  public static String VM_ARG_INCLUDE_PATHS = "includeFiles";
  private Set<Path> m_includePaths;

  @PostConstruct
  public void init() {
    List<Path> includeFileList = CONFIG.getPropertyValue(IncludeFilesProperty.class);
    if (includeFileList != null) {
      m_includePaths = includeFileList.stream().filter(p -> {
        try {
          if (!Configuration.get().getTargetModuleDirectory().equals(MigrationUtility.getModuleDirectory(p))) {
            throw new ProcessingException("Include filter file '" + p + "' is not part of the module '" + Configuration.get().getTargetModuleDirectory() + "' to migrate.");
          }
        }
        catch (IOException e) {
          throw new ProcessingException("Could not determ module directory of '" + p + "'.", e);
        }
        return true;
      }).collect(Collectors.toSet());
    }
  }

  @Override
  public boolean test(PathInfo pathInfo) {
    return m_includePaths == null || m_includePaths.contains(pathInfo.getPath());
  }

}
