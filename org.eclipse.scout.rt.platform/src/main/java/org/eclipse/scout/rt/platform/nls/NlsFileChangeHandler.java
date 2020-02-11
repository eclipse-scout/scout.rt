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

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.util.StringUtility;

public class NlsFileChangeHandler {
  private final PathMatcher m_fileMatcher;
  private final Consumer<Path> m_onFileChangeConsumer;

  public NlsFileChangeHandler(String resourceBundleName, Consumer<Path> onFileChangeConsumer) {
    String filenamePattern = StringUtility.replace(resourceBundleName, ".", "/") + "*." + NlsFileWatcher.TEXT_RESOURCE_EXTENSION;
    m_fileMatcher = FileSystems.getDefault().getPathMatcher("glob:**/" + filenamePattern);
    m_onFileChangeConsumer = onFileChangeConsumer;
  }

  public void fileChanged(Path path) {
    if (m_onFileChangeConsumer != null & m_fileMatcher.matches(path)) {
      m_onFileChangeConsumer.accept(path);
    }
  }
}
