/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides functionality to authenticate an ongoing HTTP request. Typically, an authenticator consists of two parts:
 * the authentication method to challenge the client to provide credentials, and the credential verifier to verify the
 * user's credentials against a data source.
 * <p>
 * This authenticator is designed to be used in {@link Filter Servlet filter}.
 *
 * @since 5.2
 */
public interface IAuthenticator {

  /**
   * Invoke to authenticate the given {@link HttpServletRequest}.
   * <p>
   * In case the request was handled by this authenticator, the caller should exit the chain. However, the term
   * 'handled' is not equals to successful authentication, it simply means, that this authenticator handled the request
   * and that there is no further action required by the caller. Typically, if this method returns <code>true</code>,
   * this authenticator continues chain, or sets according headers otherwise.
   *
   * @return <code>true</code> if the request was handled (caller should exit chain), or <code>false</code> if nothing
   *         was done (caller should continue by invoking subsequent authenticators).
   */
  boolean handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;

  /**
   * Invoke to destroy this authenticator in order to free resources.
   */
  void destroy();
}
