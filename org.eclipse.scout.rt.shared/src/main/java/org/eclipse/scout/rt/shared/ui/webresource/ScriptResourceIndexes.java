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
package org.eclipse.scout.rt.shared.ui.webresource;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.LazyValue;

@ApplicationScoped
public class ScriptResourceIndexes {

  private final FinalValue<Map<String, String>> m_index = new FinalValue<>();
  private static final LazyValue<ScriptResourceIndexes> INSTANCE = new LazyValue<>(ScriptResourceIndexes.class);
  public static final String INDEX_FILE_NAME = "file-list";

  public static String getMinifiedPath(String path) {
    return INSTANCE.get().get(path);
  }

  public String get(String path) {
    Map<String, String> index = m_index.setIfAbsentAndGet(this::createNewIndex);
    String indexValue = index.get(AbstractWebResourceResolver.stripLeadingSlash(path));
    if (indexValue == null) {
      return path; // return the input if no mapping could be found
    }
    return indexValue;
  }

  protected Map<String, String> createNewIndex() {
    try {
      Enumeration<URL> fileListEnum = getClass().getClassLoader().getResources(AbstractWebResourceResolver.MIN_FOLDER_NAME + '/' + INDEX_FILE_NAME);
      return BEANS.get(ScriptResourceIndexBuilder.class).build(fileListEnum);
    }
    catch (IOException e) {
      throw new ProcessingException("Error loading {}", INDEX_FILE_NAME, e);
    }
  }
}
