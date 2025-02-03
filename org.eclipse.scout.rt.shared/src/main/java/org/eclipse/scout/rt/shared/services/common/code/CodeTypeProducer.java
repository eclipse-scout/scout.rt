/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;

/**
 * Producer for code type instances. Simply delegates to {@link ICodeService}
 */
public class CodeTypeProducer implements IBeanInstanceProducer<ICodeType<?, ?>> {

  @Override
  public ICodeType<?, ?> produce(IBean<ICodeType<?, ?>> bean) {
    return BEANS.get(ICodeService.class).getCodeType(bean.getBeanClazz());
  }
}
