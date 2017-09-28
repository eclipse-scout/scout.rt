/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.pwd;

import org.eclipse.scout.rt.platform.exception.VetoException;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link DefaultPasswordPolicyTest}</h3>
 */
public class DefaultPasswordPolicyTest {

  @Test
  public void testPolicy() {
    IPasswordPolicy policy = new DefaultPasswordPolicy();
    assertVetoException(policy, "11", "whatever".toCharArray(), "uname", 1);
    assertVetoException(policy, "11", "whatever".toCharArray(), "uname", 0);
    assertVetoException(policy, "11", "11uname22_".toCharArray(), "uname", -1);
    assertVetoException(policy, "11", "11uname22_".toCharArray(), "UNAME", -1);
    assertVetoException(policy, "11", null, "uname", -1);
    assertVetoException(policy, "11", "".toCharArray(), "uname", -1);
    assertVetoException(policy, "11", "whatever".toCharArray(), "uname", -1);
    assertVetoException(policy, "11", "123456789".toCharArray(), "uname", -1);
    assertVetoException(policy, "11", "1234567ABC".toCharArray(), "uname", -1);

    policy.check("11", "whatever1_".toCharArray(), "uid", -1);
    policy.check("11", "whatever1_".toCharArray(), null, -1);
  }

  private void assertVetoException(IPasswordPolicy policy, String userId, char[] newPassword, String userName, int historyIndex) {
    boolean hasException = false;
    try {
      policy.check(userId, newPassword, userName, historyIndex);
    }
    catch (VetoException e) {
      hasException = true;
    }
    Assert.assertTrue(hasException);
  }
}
