/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.services.common.pwd;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.PasswordPolicy;

/**
 * @deprecated in 11.0, moved to {@link PasswordPolicy}
 */
@Deprecated
public class DefaultPasswordPolicy implements IPasswordPolicy {

  @Override
  public String getText() {
    return BEANS.get(PasswordPolicy.class).getText();
  }

  @Override
  public void check(String userId, char[] newPassword, String userName, int historyIndex) {
    BEANS.get(PasswordPolicy.class).check(userName, newPassword, historyIndex);
  }
}
