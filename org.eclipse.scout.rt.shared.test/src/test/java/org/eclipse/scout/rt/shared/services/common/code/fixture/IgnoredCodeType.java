/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code.fixture;

import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

@IgnoreBean
public class IgnoredCodeType extends AbstractCodeType<Long, String> {
  private static final long serialVersionUID = 1L;

  public static final Long ID = Long.valueOf(42);

  @Override
  public Long getId() {
    return ID;
  }
}
