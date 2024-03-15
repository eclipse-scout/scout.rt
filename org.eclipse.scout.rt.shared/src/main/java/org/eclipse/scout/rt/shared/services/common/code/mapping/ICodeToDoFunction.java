/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code.mapping;

import org.eclipse.scout.rt.api.data.code.CodeDo;
import org.eclipse.scout.rt.dataobject.mapping.IToDoFunction;
import org.eclipse.scout.rt.shared.services.common.code.ICode;

/**
 * {@link IToDoFunction} that converts an {@link ICode} to a {@link CodeDo}.
 */
public interface ICodeToDoFunction extends IToDoFunction<ICode<?>, CodeDo> {
}
