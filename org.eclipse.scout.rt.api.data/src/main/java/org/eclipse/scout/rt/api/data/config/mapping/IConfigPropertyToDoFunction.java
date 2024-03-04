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

import org.eclipse.scout.rt.api.data.config.AbstractApiExposedConfigPropertyContributor;
import org.eclipse.scout.rt.api.data.config.ConfigPropertyDo;
import org.eclipse.scout.rt.dataobject.mapping.IToDoFunction;
import org.eclipse.scout.rt.platform.config.IConfigProperty;

/**
 * Function that converts an {@link IConfigProperty} to a {@link ConfigPropertyDo}. Typically
 * {@link AbstractApiExposedConfigPropertyContributor#create(IConfigProperty)} can be used to call the conversion.
 */
public interface IConfigPropertyToDoFunction extends IToDoFunction<IConfigProperty<?>, ConfigPropertyDo> {
}
