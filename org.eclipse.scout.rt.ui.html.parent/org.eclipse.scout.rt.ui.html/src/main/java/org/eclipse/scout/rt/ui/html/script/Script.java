/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.script;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletContext;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Scout JEE script resources are placed inside the jar in the folder <code>/META-INF/resources</code> as described in
 * {@link ServletContext#getResource(String)}.
 */
public class Script {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(Script.class);

  public static enum FileType {
    JS,
    CSS
  }

  public static enum NodeType {
    /**
     * A macro consists of multiple libraries and modules and has the file name *-macro.js or *-macro.css
     * <p>
     * Typically placed inside <code>/META-INF/resources/WebContent/res</code>
     * <p>
     * Example: the request to <code>/res/crm-14.0.0.min.js</code> is mapped to the file
     * <code>/META-INF/resources/WebContent/res/crm-macro.js</code> which has include directives
     * <code>//@include("jquery-4.0.0.min.js")</code> and <code>//@include("scout-5.0.0.min.js")</code>
     */
    MACRO,

    /**
     * A library or thirdparty library is available in minimized and/or non-minimized form with the file name pattern
     * *.min.js
     * or *.min.css respectively *.js or *.css
     * <p>
     * Typically placed inside <code>/META-INF/resources/WebContent/res</code>
     * <p>
     * Example: the request to <code>/res/jquery-4.0.0.min.js</code> is mapped to the minimized file
     * <code>/META-INF/resources/WebContent/res/jquery-4.0.0.min.js</code> and the non-minimized file
     * <code>/META-INF/resources/WebContent/res/jquery-4.0.0.js</code>
     */
    LIBRARY,

    /**
     * A source module is a project javascript file that is being developed, it may consist of multiple fragments. The
     * file name pattern is *-module.js or *-module.css
     * <p>
     * Placed inside <code>/META-INF/resources/js</code>
     * <p>
     * Example: the request to <code>/res/scout-5.0.0.min.js</code> is mapped to the file
     * <code>/META-INF/resources/js/scout-module.js</code> which has include directives
     * <code>//@include("tree/Tree.js")</code> and <code>//@include("table/Table.js")</code>
     */
    SRC_MODULE,

    /**
     * Fragments are the individual parts of a module
     * <p>
     * Placed inside <code>/META-INF/resources/js</code>
     * <p>
     * Example: the include <code>tree/Tree.js</code> is mapped to the file
     * <code>/META-INF/resources/js/tree/Tree.js</code>
     */
    SRC_FRAGMENT
  }

  private final String m_requestPath;
  private final URL m_url;
  private final FileType m_fileType;
  private final NodeType m_nodeType;

  public Script(String requestPath, URL url, Script.FileType fileType, Script.NodeType nodeType) {
    if (url == null) {
      throw new IllegalArgumentException(requestPath + ": url is null");
    }
    m_requestPath = requestPath;
    m_url = url;
    m_fileType = fileType;
    m_nodeType = nodeType;
  }

  public String getRequestPath() {
    return m_requestPath;
  }

  public URL getURL() {
    return m_url;
  }

  public Script.FileType getFileType() {
    return m_fileType;
  }

  public Script.NodeType getNodeType() {
    return m_nodeType;
  }

  public String getContentUTF8() throws IOException {
    try (InputStream in = m_url.openStream()) {
      return IOUtility.getContentUtf8(in);
    }
    catch (ProcessingException e) {
      LOG.warn("reading " + m_url, e);
      throw new IOException(e.getMessage());
    }
  }

  public byte[] getContentRaw() throws IOException {
    try (InputStream in = m_url.openStream()) {
      return IOUtility.getContent(in);
    }
    catch (ProcessingException e) {
      LOG.warn("reading " + m_url, e);
      throw new IOException(e.getMessage());
    }
  }

}
