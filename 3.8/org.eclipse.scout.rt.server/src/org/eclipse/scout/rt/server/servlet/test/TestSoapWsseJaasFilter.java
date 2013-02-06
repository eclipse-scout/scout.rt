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
package org.eclipse.scout.rt.server.servlet.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.server.SoapWsseJaasFilter;

public class TestSoapWsseJaasFilter {

  private static class TestFilter extends SoapWsseJaasFilter {
    @Override
    public Subject parseSubject(InputStream httpIn, ByteArrayOutputStream cacheOut) throws Exception {
      return super.parseSubject(httpIn, cacheOut);
    }
  }

  public static void main(String[] args) throws Exception {
    TestFilter filter = new TestFilter();
    filter.init(null);
    InputStream httpIn = TestSoapWsseJaasFilter.class.getResourceAsStream("test-request.xml");
    ByteArrayOutputStream cacheOut = new ByteArrayOutputStream();
    Subject subject = filter.parseSubject(httpIn, cacheOut);
    System.out.println("subject: " + subject);
  }
}
