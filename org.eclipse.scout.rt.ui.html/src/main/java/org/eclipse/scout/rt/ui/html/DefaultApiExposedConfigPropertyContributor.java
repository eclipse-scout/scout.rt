/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.api.data.config.AbstractApiExposedConfigPropertyContributor;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.PlatformDevModeProperty;

/**
 * Contributor for {@link IConfigProperty} instances which are exposed from the UI by default.
 */
public class DefaultApiExposedConfigPropertyContributor extends AbstractApiExposedConfigPropertyContributor {
  @Override
  protected Collection<? extends IConfigProperty<?>> getExposedProperties() {
    List<IConfigProperty<?>> result = new ArrayList<>();
    result.add(BEANS.get(PlatformDevModeProperty.class));
    return result;
  }
}
