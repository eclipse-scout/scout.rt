/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.session;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Listener to be notified about all session state changes.
 * <p>
 * No manual registration required, only implement interface.
 * <p>
 * If a specific session listener is required, use {@link ISessionListener} instead.
 *
 * @since 5.1
 */
@FunctionalInterface
@ApplicationScoped
public interface IGlobalSessionListener extends ISessionListener {

}
