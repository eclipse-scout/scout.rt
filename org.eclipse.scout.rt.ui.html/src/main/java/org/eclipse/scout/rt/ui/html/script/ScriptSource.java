/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.util.ToStringBuilder;

public class ScriptSource {

  public static enum FileType {
    JS,
    CSS,
    OTHER;

    public static FileType resolveFromFilename(String filename) {
      if (filename != null) {
        if (filename.endsWith(".js")) {
          return JS;
        }
        if (filename.endsWith(".css")) {
          return CSS;
        }
      }
      return OTHER;
    }
  }

  public static enum NodeType {
    /**
     * A macro consists of multiple libraries and modules and has the file name *-macro.js or *-macro.css
     * <p>
     * Typically placed inside <code>/src/main/resources/WebContent/res</code>
     * <p>
     * Example: the request to <code>/res/myproject-d5ab7846.min.js</code> is mapped to the file
     * <code>/src/main/resources/WebContent/res/myproject-macro.js</code> which has include directives
     * <code>//@include("jquery-module.js")</code> and <code>//@include("scout-module.js")</code>
     */
    MACRO,

    /**
     * A library or third-party library in non-minimized form with the file name pattern *.js or *.css. If only the
     * minimized version of a library is found (*.min.js or *.min.css, respectively), is is returned even when
     * <i>minify=false</i>.
     * <p>
     * Typically placed inside <code>/src/main/resources/WebContent/res</code>
     * <p>
     * Example: the request to <code>/res/lib.min.js</code> is mapped to the file
     * <code>/src/main/resources/WebContent/res/lib.js</code> (preferred) or
     * <code>/src/main/resources/WebContent/res/lib.min.js</code> (fallback).
     *
     * @see {@link #LIBRARY_MINIFIED}.
     */
    LIBRARY,

    /**
     * A library or third-party library in minimized form with the file name pattern *.min.js or *.min.css. If only the
     * non-minimized version of a library is found (*.js or *.css, respectively), is is returned even when
     * <i>minify=true</i>.
     * <p>
     * Typically placed inside <code>/src/main/resources/WebContent/res</code>
     * <p>
     * Example: the request to <code>/res/lib.min.js</code> is mapped to the file
     * <code>/src/main/resources/WebContent/res/lib.min.js</code> (preferred) or
     * <code>/src/main/resources/WebContent/res/lib.js</code> (fallback).
     *
     * @see {@link #LIBRARY}.
     */
    LIBRARY_MINIFIED,

    /**
     * A source module is a project javascript file that is being developed, it may consist of multiple fragments. The
     * file name pattern is *-module.js or *-module.css
     * <p>
     * Typically placed inside <code>/src/main/js</code>
     * <p>
     * Example: the request to <code>/res/scout.js</code> is mapped to the file
     * <code>/src/main/js/scout-module.js</code> which has include directives
     * <code>//@include("scout/tree/Tree.js")</code> and <code>//@include("scout/table/Table.js")</code>
     */
    SRC_MODULE,

    /**
     * Fragments are the individual parts of a module
     * <p>
     * Typically placed inside <code>/src/main/js/...</code>
     * <p>
     * Example: the include <code>scout/tree/Tree.js</code> is mapped to the file
     * <code>/src/main/js/scout/tree/Tree.js</code>
     */
    SRC_FRAGMENT,

    /**
     * Fall-back type to prevent NPE in switch statements.
     */
    OTHER
  }

  private final String m_requestPath;
  private final URL m_url;
  private final FileType m_fileType;
  private final NodeType m_nodeType;

  public ScriptSource(String requestPath, URL url, ScriptSource.FileType fileType, ScriptSource.NodeType nodeType) {
    if (url == null) {
      throw new IllegalArgumentException(requestPath + ": url is null");
    }
    m_requestPath = requestPath;
    m_url = url;
    m_fileType = (fileType == null ? FileType.OTHER : fileType);
    m_nodeType = (nodeType == null ? NodeType.OTHER : nodeType);
  }

  /**
   * Like {@link #ScriptSource(String, URL, FileType, NodeType)} but resolves the file type automatically from the
   * requestPath
   */
  public ScriptSource(String requestPath, URL url, ScriptSource.NodeType nodeType) {
    this(requestPath, url, FileType.resolveFromFilename(requestPath), nodeType);
  }

  public String getRequestPath() {
    return m_requestPath;
  }

  public URL getURL() {
    return m_url;
  }

  /**
   * Returns never <code>null</code>.
   */
  public ScriptSource.FileType getFileType() {
    return m_fileType;
  }

  /**
   * Returns never <code>null</code>.
   */
  public ScriptSource.NodeType getNodeType() {
    return m_nodeType;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attr("requestPath", m_requestPath)
        .attr("url", m_url)
        .attr("fileType", m_fileType)
        .attr("nodeType", m_nodeType)
        .toString();
  }
}
