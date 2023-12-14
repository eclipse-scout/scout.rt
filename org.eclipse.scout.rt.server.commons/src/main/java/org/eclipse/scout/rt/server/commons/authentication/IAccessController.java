/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.security.ICredentialVerifier;

/**
 * Provides functionality to control access to resources only accessible for authenticated users.
 * <p>
 * An {@link IAccessController} consists of the following 3 parts:
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
   * Allows to inform the {@link org.eclipse.scout.rt.ui.html.IUiSession} that a new Subject is created by an
   * {@link IAccessController} that should be updated on the {@link org.eclipse.scout.rt.client.IClientSession}
   */
  String UPDATED_SUBJECT = "scout.authentication.updatedSubject";

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
