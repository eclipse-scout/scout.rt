/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.commons.servlet;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Interface for common {@link HttpProxyRequestOptions} modifiers.
 */
@ApplicationScoped
public interface IHttpProxyRequestOptionsModifier {

  /**
   * Modifies the given {@link HttpProxyRequestOptions}.
   *
   * @param options
   *          to modify
   * @param context
   *          of the given {@link HttpProxyRequestOptions}
   */
  void modify(HttpProxyRequestOptions options, HttpProxyRequestContext context);
}
