/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
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
@ApplicationScoped
public interface IGlobalSessionListener extends ISessionListener {

}
