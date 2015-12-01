/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension;

import org.eclipse.scout.rt.platform.IOrdered;

/**
 * Marker interface used for moving an ordered object to its root. The semantics of root is context specific (e.g. the
 * root of an menu is the root menu list whereas the root of a form field is the form's main box).
 * <p/>
 * Instead of using this class, consider to use {@link IExtensionRegistry#registerMoveToRoot(Class, double)}.
 */
public interface IMoveModelObjectToRootMarker extends IOrdered {

}
