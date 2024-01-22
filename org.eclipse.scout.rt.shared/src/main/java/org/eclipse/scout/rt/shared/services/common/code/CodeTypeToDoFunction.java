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
import org.eclipse.scout.rt.platform.Order;

/**
 * Default function that converts a {@link ICodeType} to a {@link CodeTypeDo}.
 */
@Order(5050) // last converter for all remaining CodeTypes
public class CodeTypeToDoFunction extends AbstractCodeTypeToDoFunction<ICodeType<?, ?>, CodeTypeDo> {
}
