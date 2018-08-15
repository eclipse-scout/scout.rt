/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
