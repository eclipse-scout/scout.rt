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
    assertVetoException(policy, "11", "whatEver".toCharArray(), "uname", 1);
    assertVetoException(policy, "11", "whatEver".toCharArray(), "uname", 0);
    assertVetoException(policy, "11", "11unameA22_".toCharArray(), "uname", -1);
    assertVetoException(policy, "11", "11unameA22_".toCharArray(), "UNAME", -1);
    assertVetoException(policy, "11", null, "uname", -1);
    assertVetoException(policy, "11", "".toCharArray(), "uname", -1);
    assertVetoException(policy, "11", "whatEver".toCharArray(), "uname", -1);
    assertVetoException(policy, "11", "123456789".toCharArray(), "uname", -1);
    assertVetoException(policy, "11", "1234567ABCabc".toCharArray(), "uname", -1);
    assertVetoException(policy, "11", "WHATEVER1_".toCharArray(), "uname", -1);

    policy.check("11", "whatEver1_".toCharArray(), "uid", -1);
    policy.check("11", "whatEver1_".toCharArray(), null, -1);
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
