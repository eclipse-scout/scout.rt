/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;

/**
 * Resolve mime types using {@link Files#probeContentType(Path)}
 * <p>
 * see {@link IMimeTypeDetector}
 *
 * @author BSI AG
 * @since 5.2
 */
@Order(IOrdered.DEFAULT_ORDER)
@ApplicationScoped
public class JavaNioMimeTypeDetector implements IMimeTypeDetector {

  @Override
  public String getMimeType(Path path) {
    try {
      return Files.probeContentType(path);
    }
    catch (IOException e) { // NOSONAR
      //ignore
    }
    return null;
  }
}
