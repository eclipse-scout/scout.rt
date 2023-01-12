/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.util.EventListener;

/**
 * marker interface for weak event listener WeakEventListener's are removed from scout's models automatically when their
 * reference (WeakReference) is null. NOTE: weak listeners should be referenced by their host since the model will not
 * hold a valid reference to weak objects. best practice is to have a member variable referencing every weak listener.
 */

public interface WeakEventListener extends EventListener {

}
