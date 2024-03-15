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
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.services.common.code.ICode;

/**
 * Default function that converts a {@link ICode} to a {@link CodeDo}.
 */
@Order(5050) // last converter for all remaining CodeTypes
public class CodeToDoFunction extends AbstractCodeToDoFunction<ICode<?>, CodeDo> {
}
