/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.script;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.scout.rt.ui.html.res.IWebContentService;
import org.eclipse.scout.rt.ui.html.script.ScriptSource.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptFileLocator {
  private static final Logger LOG = LoggerFactory.getLogger(ScriptFileLocator.class);

  private final IWebContentService m_resourceLocator;

  public ScriptFileLocator(IWebContentService resourceLocator) {
    m_resourceLocator = resourceLocator;
  }

  protected ScriptSource locateFile(String requestPath, Matcher mat, boolean minified, boolean lenient) {
    // group(3) = fingerprint (not used here)
    String parent = mat.group(1);
    String fileName = mat.group(2);
    String fileExtension = mat.group(4);

    // macros are loaded with and without suffix -macro
    FileLookup macro = getMacroFileLookup(parent, fileName, fileExtension);

    // modules are loaded with and without suffix -module
    List<FileLookup> modules = getModuleFileLookup(fileName, fileExtension, lenient);

    // only (external) libraries are available minified and non-minified
    // all other files are always non-minified on the classpath
    FileLookup library = new FileLookup(true, parent + fileName + "." + fileExtension, NodeType.LIBRARY);
    FileLookup libraryMinified = new FileLookup(true, parent + fileName + ".min." + fileExtension, NodeType.LIBRARY_MINIFIED);

    List<FileLookup> lookups = new ArrayList<>(5);
    lookups.add(macro);
    lookups.addAll(modules);
    lookups.add(minified ? libraryMinified : library);
    lookups.add(minified ? library : libraryMinified);

    for (FileLookup lookup : lookups) {
      if (lookup.lookup()) {
        return lookup.toScriptSource(requestPath);
      }
    }

    // when no file matches
    LOG.warn("locate {}: does not exist (no library, macro or source module)", requestPath);
    return null;
  }

  protected FileLookup getMacroFileLookup(String parent, String fileName, String fileExtension) {
    fileExtension = resolveFileExtension(fileExtension);
    String lookupFileName;
    if (fileName.endsWith("-macro")) {
      lookupFileName = parent + fileName + "." + fileExtension;
    }
    else {
      lookupFileName = parent + fileName + "-macro." + fileExtension;
    }
    return new FileLookup(true, lookupFileName, NodeType.MACRO);
  }

  /**
   * When a stylesheet module is requested, we must look for .css AND .less files. This is required because the browser
   * requests always *.css, and a *.less file must be loaded instead. But a macro can also contain simple *.css files.
   * So this method must lookup both.
   */
  protected List<FileLookup> getModuleFileLookup(String fileName, String fileExtension, boolean lenient) {
    List<FileLookup> lookups = new ArrayList<>();
    if (lenient && isCss(fileExtension)) {
      lookups.add(getModuleFileLookup(fileName, "less"));
    }
    lookups.add(getModuleFileLookup(fileName, fileExtension));
    return lookups;
  }

  protected FileLookup getModuleFileLookup(String fileName, String fileExtension) {
    String lookupFileName;
    if (fileName.endsWith("-module")) {
      lookupFileName = fileName + "." + fileExtension;
    }
    else {
      lookupFileName = fileName + "-module." + fileExtension;
    }
    return new FileLookup(false, lookupFileName, NodeType.SRC_MODULE);
  }

  /**
   * When a stylesheet macro is requested by the browser, it request a resource with a '.css' file extension. On the
   * server we must actually load a '.less' file for that macro, that's why we must switch the file extension in this
   * method. We don't do this for modules and fragments.
   */
  protected String resolveFileExtension(String extension) {
    if (isCss(extension)) {
      return "less";
    }
    return extension;
  }

  protected boolean isCss(String extension) {
    return "css".equals(extension);
  }

  protected class FileLookup {
    private final boolean m_webContent;
    private final String m_fileName;
    private final NodeType m_nodeType;
    private URL m_url;

    protected FileLookup(boolean webContent, String fileName, NodeType nodeType) {
      m_webContent = webContent;
      m_fileName = fileName;
      m_nodeType = nodeType;
    }

    protected boolean lookup() {
      if (m_webContent) {
        m_url = m_resourceLocator.getWebContentResource(m_fileName);
      }
      else {
        m_url = m_resourceLocator.getScriptSource(m_fileName);
      }
      return m_url != null;
    }

    protected ScriptSource toScriptSource(String requestPath) {
      return new ScriptSource(requestPath, m_url, m_nodeType);
    }
  }
}
