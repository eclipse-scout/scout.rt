/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.pwd;

import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.PasswordPolicyMinLengthProperty;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.security.PasswordPolicy;
import org.eclipse.scout.rt.testing.platform.mock.MockConfigPropertyRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class DefaultPasswordPolicyTest {

  @Rule
  public MockConfigPropertyRule<Integer> m_passwordPolicyMinLengthPropertyRule = new MockConfigPropertyRule<>(PasswordPolicyMinLengthProperty.class, 12);

  @Test
  public void testPolicy() {
    PasswordPolicy policy = new PasswordPolicy();
    assertVetoException(policy, "uname", "12characters".toCharArray(), 1);
    assertVetoException(policy, "uname", "12characters".toCharArray(), 0);
    assertVetoException(policy, "uname", "12charactersunameA22_".toCharArray(), -1);
    assertVetoException(policy, "UNAME", "12charactersunameA22_".toCharArray(), -1);
    assertVetoException(policy, "uname", null, -1);
    assertVetoException(policy, "uname", "".toCharArray(), -1);
    assertVetoException(policy, "uname", "12characters".toCharArray(), -1);
    assertVetoException(policy, "uname", "123456789012".toCharArray(), -1);
    assertVetoException(policy, "uname", "1234567ABCabc".toCharArray(), -1);
    assertVetoException(policy, "uname", "12CHARACTERS_".toCharArray(), -1);

    policy.check("uid", "12Characters_".toCharArray(), -1);
    policy.check(null, "12Characters_".toCharArray(), -1);

    m_passwordPolicyMinLengthPropertyRule.setValue(20);
    assertVetoException(policy, "uname", "13Characters_".toCharArray(), -1);
    policy.check("uname", "20CharacterPassword_".toCharArray(), -1);
  }

  private void assertVetoException(PasswordPolicy policy, String userName, char[] newPassword, int historyIndex) {
    boolean hasException = false;
    try {
      policy.check(userName, newPassword, historyIndex);
    }
    catch (VetoException e) {
      hasException = true;
    }
    Assert.assertTrue(hasException);
  }
}
