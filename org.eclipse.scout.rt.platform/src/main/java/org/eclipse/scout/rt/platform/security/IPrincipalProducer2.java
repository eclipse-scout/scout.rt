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
import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Producer for {@link Principal} objects to represent authenticated users.
 *
 * @since 10.0
 */
@FunctionalInterface
@ApplicationScoped
public interface IPrincipalProducer2 {

  /**
   * @param username
   *     or userId
   * @param params
   *     additional parameters
   * @return a principal based on the arguments
   */
  Principal produce(String username, List<String> params);
}
