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

import java.util.Set;

import org.eclipse.scout.rt.api.data.ApiExposeHelper;
import org.eclipse.scout.rt.api.data.ApiExposed;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Order;

/**
 * Default {@link IApiExposedCodeTypeContributor} that exposes all {@link ICodeType} instances which have the
 * {@link ApiExposed} annotation.
 */
@Order(-5000) // this provider should come first so that custom providers can modify the result
public class ApiExposedCodeTypeDoContributor implements IApiExposedCodeTypeContributor {
  private final ApiExposeHelper m_apiExposeHelper = BEANS.get(ApiExposeHelper.class);

  @Override
  public void contribute(Set<ICodeType> codeTypes) {
    BEANS.getBeanManager().getBeans(ICodeType.class).stream()
        .filter(this::isApiExposedCodeType)
        .map(IBean::getInstance)
        .forEach(codeTypes::add);
  }

  protected boolean isApiExposedCodeType(IBean<ICodeType> codeTypeBean) {
    return m_apiExposeHelper.hasApiExposedAnnotation(codeTypeBean);
  }
}
