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
@ApplicationScoped
public interface IGlobalSessionListener extends ISessionListener {

}
