/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.bookmark;

import org.eclipse.scout.rt.dataobject.IDoEntity;

/**
 * Marker interface for data object used to identify rows when restoring a bookmark's selection/drill down.
 * <p>
 * TODO [23.2] PBZ Remove this interface when bookmark handling was moved completely to suite layer. <br>
 * Currently this interface allows to use bookmark handling in suite by having CRM providing the full implementation at
 * runtime.
 */
public interface IBookmarkTableRowIdentifierDo extends IDoEntity {
}
