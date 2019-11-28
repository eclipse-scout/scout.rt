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

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.configuration.MigrationConfigProperties.IncludeFilesProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;

public class IncludeVmArgPathFilter implements IMigrationIncludePathFilter {
  public static final String VM_ARG_INCLUDE_PATHS = "includeFiles";
  private Set<Path> m_includePaths;

  @PostConstruct
  public void init() {
    List<Path> includeFileList = CONFIG.getPropertyValue(IncludeFilesProperty.class);
    if (includeFileList != null) {
      m_includePaths = new HashSet<>(includeFileList);
    }
  }

  @Override
  public boolean test(PathInfo pathInfo) {
    return m_includePaths == null || m_includePaths.contains(pathInfo.getPath());
  }

}
