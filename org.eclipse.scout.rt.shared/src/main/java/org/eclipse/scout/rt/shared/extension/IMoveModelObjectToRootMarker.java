/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
