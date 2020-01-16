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
package org.eclipse.scout.rt.platform.nls;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import org.eclipse.scout.rt.platform.resource.AbstractClasspathFileWatcher;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NlsFileWatcher extends AbstractClasspathFileWatcher {
  private static final Logger LOG = LoggerFactory.getLogger(NlsFileWatcher.class);
  private static final String TEXT_RESOURCE_EXTENSION = "properties";

  private final PathMatcher m_fileMatcher;
  private final PathMatcher m_directoryMatcher;
  private final Runnable m_onFileChanged;

  public NlsFileWatcher(String resourceBundleName, Runnable onFileChanged) throws IOException {
    super(false);
    int pos = StringUtility.lastIndexOf(resourceBundleName, ".");
    String filename = StringUtility.substring(resourceBundleName, pos + 1) + "*." + TEXT_RESOURCE_EXTENSION;
    String directory = StringUtility.replace(StringUtility.substring(resourceBundleName, 0, pos), ".", "/");

    m_fileMatcher = FileSystems.getDefault().getPathMatcher("glob:**/" + directory + "/" + filename);
    m_directoryMatcher = FileSystems.getDefault().getPathMatcher("glob:**/" + directory);
    m_onFileChanged = onFileChanged;

    callInitializer();
  }

  @Override
  protected String getConfiguredJobName() {
    return "Nls file watcher";
  }

  @Override
  protected boolean execAccept(Path path) {
    return m_directoryMatcher.matches(path);
  }

  @Override
  protected void execFileChanged(Path path) {
    if (m_fileMatcher.matches(path)) {
      LOG.info("File {} changed", path);
      m_onFileChanged.run();
    }
  }
}
