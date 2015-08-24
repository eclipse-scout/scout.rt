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
package org.eclipse.scout.commons;

import java.util.EventListener;

/**
 * marker interface for weak event listener WeakEventListener's are removed from scout's models automatically when their
 * reference (WeakReference) is null. NOTE: weak listeners should be referenced by their host since the model will not
 * hold a valid reference to weak objects. best practice is to have a member variable referencing every weak listener.
 */

public interface WeakEventListener extends EventListener {

}
