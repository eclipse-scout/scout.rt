/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

import org.eclipse.scout.rt.api.data.code.CodeTypeDo;
import org.eclipse.scout.rt.dataobject.mapping.IToDoFunction;

/**
 * {@link IToDoFunction} that converts an {@link ICodeType} to a {@link CodeTypeDo}.
 */
public interface ICodeTypeToDoFunction extends IToDoFunction<ICodeType<?, ?>, CodeTypeDo> {
}
