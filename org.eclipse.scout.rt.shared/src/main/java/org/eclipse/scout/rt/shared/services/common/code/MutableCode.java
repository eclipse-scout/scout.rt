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

import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("7454507c-ec9f-4ba0-945b-2bd161b7ffb9")
public class MutableCode<T> extends AbstractCode<T> {
  private static final long serialVersionUID = 1L;

  public MutableCode(ICodeRow<T> row) {
    super(row);
  }
}
