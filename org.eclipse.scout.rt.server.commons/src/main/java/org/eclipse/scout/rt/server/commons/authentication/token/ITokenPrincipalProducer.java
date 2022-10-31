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
   *          all parts of the token
   * @return a principal based on the token parts
   */
  Principal produce(List<byte[]> tokenParts);
}
