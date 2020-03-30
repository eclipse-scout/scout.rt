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

import java.util.Map;
import java.util.Optional;

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
    // TODO This is not sufficient. Index-File is cached -> It should be reloaded when bundles change -> Should consider cache=false resp. clearCache request parameters
    // TODO But even if index file would be reloaded it won't work because it would contain duplicate values, even though dist folder would be cleared before build... I don't know why
    Optional<WebResourceDescriptor> desc = WebResources.resolveIndexFile(INDEX_FILE_NAME);
    if (!desc.isPresent()) {
      throw new ProcessingException("Error loading {}", INDEX_FILE_NAME);
    }
    return BEANS.get(ScriptResourceIndexBuilder.class).build(desc.get().getUrl());
  }
}
