/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.popup;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class PopupTest {

  @Test
  public void testPopupDisposeOnAnchorDispose() {

    // dispose current anchor -> popup is closed

    MyPopup popup = new MyPopup();
    MyStringField anchor = new MyStringField();
    popup.setAnchor(anchor);
    popup.open();

    assertTrue(IDesktop.CURRENT.get().getAddOn(PopupManager.class).getPopups().contains(popup));
    assertFalse(popup.isDisposeDone());
    assertFalse(anchor.isDisposeDone());

    anchor.dispose();

    assertFalse(IDesktop.CURRENT.get().getAddOn(PopupManager.class).getPopups().contains(popup));
    assertTrue(popup.isDisposeDone());
    assertTrue(anchor.isDisposeDone());

    // dispose former anchor -> popup is not closed

    popup = new MyPopup();
    anchor = new MyStringField();
    popup.setAnchor(anchor);
    popup.open();

    assertTrue(IDesktop.CURRENT.get().getAddOn(PopupManager.class).getPopups().contains(popup));
    assertFalse(popup.isDisposeDone());
    assertFalse(anchor.isDisposeDone());

    popup.setAnchor(null);
    anchor.dispose();

    assertTrue(IDesktop.CURRENT.get().getAddOn(PopupManager.class).getPopups().contains(popup));
    assertFalse(popup.isDisposeDone());
    assertTrue(anchor.isDisposeDone());
  }

  protected static class MyStringField extends AbstractStringField {
  }

  protected static class MyPopup extends AbstractPopup {
  }
}
