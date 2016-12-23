/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.code;

import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("7454507c-ec9f-4ba0-945b-2bd161b7ffb9")
public class MutableCode<T> extends AbstractCode<T> {
  private static final long serialVersionUID = 1L;

  public MutableCode(ICodeRow<T> row) {
    super(row);
  }
}
