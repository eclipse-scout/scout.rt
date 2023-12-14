/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons;

import java.nio.file.Path;

import jakarta.servlet.ServletContext;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.IMimeTypeDetector;

/**
 * Resolve mime types using the servlet context
 * <p>
 * In tomcat this is the conf/web.xml
 * <p>
 * see {@link IMimeTypeDetector}
 *
 * @author BSI AG
 * @since 5.2
 */
@Order(10)
@ApplicationScoped
public class ServletContextMimeTypeDetector implements IMimeTypeDetector {

  @Override
  public String getMimeType(Path path) {
    if (path == null) {
      return null;
    }
    ServletContext servletContext = BEANS.opt(ServletContext.class);
    if (servletContext == null) {
      return null;
    }
    Path fileName = path.getFileName();
    if (fileName == null) {
      return null;
    }
    String name = fileName.toString();
    return servletContext.getMimeType(name);
  }
}
