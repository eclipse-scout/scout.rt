/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.services.common.exceptionhandler;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.RemoteSystemUnavailableException;
import org.junit.Test;

public class ErrorPopupTest {

  @Test
  public void testIsNetError() {
    ErrorPopup popup = BEANS.get(ErrorPopup.class);

    assertTrue(popup.isNetError(new RemoteSystemUnavailableException("error")));
    assertTrue(popup.isNetError(new ConnectException("error")));
    assertTrue(popup.isNetError(new NoRouteToHostException("error")));
    assertTrue(popup.isNetError(new SocketException("error")));
    assertTrue(popup.isNetError(new UnknownHostException("error")));

    assertFalse(popup.isNetError(new IOException("error")));
  }

  @Test
  public void testEnsureParsed() {
    ErrorPopup popup = BEANS.get(ErrorPopup.class);
    Throwable cause = new ConnectException();
    popup.ensureErrorParsed(new Exception(new Throwable(cause)));

    // assert cause was found as parsed error
    assertEquals(cause, popup.getParsedError());
    assertTrue(popup.isNetError(popup.getParsedError()));

    // assert members were initialized
    assertNotNull(popup.m_header);
    assertNotNull(popup.m_body);
    assertNotNull(popup.m_html);
    assertNotNull(popup.m_yesButtonText);
    assertNotNull(popup.m_status);
  }
}
