/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.config.mapping;

import org.eclipse.scout.rt.api.data.config.ConfigPropertyDo;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.IConfigProperty;

@Order(5050) // last converter for all remaining properties
public class ConfigPropertyToDoFunction extends AbstractConfigPropertyToDoFunction<IConfigProperty<?>, ConfigPropertyDo> {
}
