/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.servlet25;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

import org.eclipse.scout.rt.server.servlet.IServletHelperService;
import org.eclipse.scout.service.AbstractService;

/**
 * {@link IServletHelperService} for servlet 2.5
 */
public class ServletHelperService extends AbstractService implements IServletHelperService {

  @Override
  public ServletInputStream createInputStream(final InputStream in) {
    return new ServletInputStream() {
      @Override
      public int read() throws IOException {
        return in.read();
      }
    };
  }

}
