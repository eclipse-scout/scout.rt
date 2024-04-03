/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.config;

import static java.util.stream.Collectors.toList;

import java.util.Collection;

import org.eclipse.scout.rt.api.data.ApiExposeHelper;
import org.eclipse.scout.rt.api.data.ApiExposed;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.config.IConfigProperty;

/**
 * Contributor that exposes all {@link IConfigProperty} instances that are annotated with {@link ApiExposed}.
 */
public class ApiExposedConfigPropertyContributor extends AbstractApiExposedConfigPropertyContributor {

  private final ApiExposeHelper m_apiExposeHelper = BEANS.get(ApiExposeHelper.class);

  @Override
  protected Collection<? extends IConfigProperty<?>> getExposedProperties() {
    return BEANS.getBeanManager()
        .getBeans(IConfigProperty.class).stream()
        .filter(this::isApiExposedConfigProperty)
        .map(b -> (IConfigProperty<?>) b.getInstance())
        .collect(toList());
  }

  protected boolean isApiExposedConfigProperty(IBean<IConfigProperty> configPropertyBean) {
    return m_apiExposeHelper.hasApiExposedAnnotation(configPropertyBean);
  }
}
