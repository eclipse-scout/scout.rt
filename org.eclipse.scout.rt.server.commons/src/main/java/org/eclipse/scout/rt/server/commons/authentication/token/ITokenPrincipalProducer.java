/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.authentication.token;

import java.security.Principal;
import java.util.List;

/**
 * Producer for {@link Principal} objects to represent authenticated users.
 */
@FunctionalInterface
public interface ITokenPrincipalProducer {

  /**
   * @param tokenParts
   *     all parts of the token
   * @return a principal based on the token parts
   */
  Principal produce(List<byte[]> tokenParts);
}
