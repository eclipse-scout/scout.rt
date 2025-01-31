/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.uuid;

import java.util.UUID;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * A provider for UUIDs.
 * <p>
 * Consistently using the UUID provider enables to test code which uses a random UUID in its operations. Use Scout
 * TestingUtility to mock this interface and provide a fixed UUID.
 */
@ApplicationScoped
public interface IUuidProvider {

  UUID createUuid();
}
