/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security.mapping;

import org.eclipse.scout.rt.api.data.security.PermissionDo;
import org.eclipse.scout.rt.dataobject.mapping.IToDoFunction;
import org.eclipse.scout.rt.security.IPermission;

public interface IToPermissionDoFunction extends IToDoFunction<IPermission, PermissionDo> {
}
