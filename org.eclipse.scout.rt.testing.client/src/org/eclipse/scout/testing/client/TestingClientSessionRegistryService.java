/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

package org.eclipse.scout.testing.client;

import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;

/**
 * Deprecated: use {@link org.eclipse.scout.rt.testing.client.TestingClientSessionRegistryService} instead
 * will be removed with the L-Release.
 */
@Deprecated
public class TestingClientSessionRegistryService extends org.eclipse.scout.rt.testing.client.TestingClientSessionRegistryService {

  /**
   * @param delegate
   */
  public TestingClientSessionRegistryService(IClientSessionRegistryService delegate) {
    super(delegate);
  }
}
