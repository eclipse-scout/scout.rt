/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import java.security.Principal;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Producer for {@link Principal} objects to represent authenticated users.
 * <p>
 * See also {@link IPrincipalProducer2}
 *
 * @since 5.2
 */
@FunctionalInterface
@ApplicationScoped
public interface IPrincipalProducer {

  /**
   * @param username
   *     or userId
   * @return a principal based on the username
   */
  Principal produce(String username);
}
