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
package org.eclipse.scout.rt.server.servlet;

import java.io.InputStream;

import javax.servlet.ServletInputStream;

import org.eclipse.scout.service.IService;

/**
 * Service with helper methods to allow different implementations for different servlet versions.
 */
public interface IServletHelperService extends IService {

  /**
   * Creates {@link ServletInputStream} with data from {@link InputStream}
   *
   * @param in
   *          {@link InputStream}
   */
  ServletInputStream createInputStream(final InputStream in);

}
