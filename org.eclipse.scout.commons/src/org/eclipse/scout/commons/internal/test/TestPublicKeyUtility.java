/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.internal.test;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.PublicKeyUtility;

public final class TestPublicKeyUtility {

  private TestPublicKeyUtility() {
  }

  public static void main(String[] args) throws Exception {
    byte[][] tmp = PublicKeyUtility.createKeyPair(null, 1024);
    byte[] publicKey = tmp[0];
    byte[] privateKey = tmp[1];
    System.out.println("public key: " + Base64Utility.encode(publicKey));
    System.out.println("private key:" + Base64Utility.encode(privateKey));
    byte[] data = "Hello World".getBytes();
    //
    byte[] sig = PublicKeyUtility.sign(data, privateKey, null, null);
    System.out.println("signature:  " + Base64Utility.encode(sig));
    boolean verify = PublicKeyUtility.verify(data, publicKey, sig, null, null);
    System.out.println("verify: " + verify);
  }
}
