/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.Bean;

/**
 * Provides functionality to control access to resources only accessible for authenticated users.
 * <p>
 * An {@link IAuthenticator} consists of the following 3 parts:
 * <ol>
 * <li>Reads the user's credentials from the request, or challenges the client to provide credentials</li>
 * <li>Verifies the user's credentials, typically by delegating to a {@link ICredentialVerifier}</li>
 * <li>Depending to the verification outcome, sets appropriate HTTP status code, continues the filter-chain on behalf of
 * the authenticated user, sets authenticated related hints onto HTTP session to be evaluated by
 * {@link TrivialAccessController} for faster authentication of subsequent requests of that user</li>
 * </ol>
 * <p>
 * Access controllers are designed to be used in {@link Filter Servlet filter}.
 *
 * @since 5.1
 */
@Bean
public interface IAccessController {

  /**
   * Invoke to authenticate the given {@link HttpServletRequest}.
   * <p>
   * In case the request was handled by this authenticator, the caller should exit the chain. However, the term
   * 'handled' is not equals to successful authentication, it simply means, that this authenticator understood the
   * request and that there is no further action required by the caller. Typically, if this method returns
   * <code>true</code>, this authenticator continues chain, or sets according headers otherwise.
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
