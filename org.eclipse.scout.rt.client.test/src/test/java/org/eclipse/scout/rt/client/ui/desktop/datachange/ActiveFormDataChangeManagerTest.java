/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.datachange;

import static org.eclipse.scout.rt.platform.util.Assertions.assertTrue;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ActiveFormDataChangeManagerTest {

  private static final String TEST_DATA_TYPE = "testDataType";

  @Test
  public void testActiveFormDataChangeManager(){
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();

    IDataChangeListener formOneChangeListenerMock = Mockito.mock(IDataChangeListener.class);
    IDataChangeListener formTwoChangeListenerMock = Mockito.mock(IDataChangeListener.class);

    TestForm formOne = new TestForm();
    BEANS.get(ActiveFormDataChangeManager.class).add(formOne, formOneChangeListenerMock, true, TEST_DATA_TYPE);
    formOne.start();

    desktop.dataChanged(TEST_DATA_TYPE);
    Mockito.verify(formOneChangeListenerMock).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));
    Mockito.verify(formTwoChangeListenerMock, Mockito.never()).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));

    TestForm formTwo = new TestForm();
    BEANS.get(ActiveFormDataChangeManager.class).add(formTwo, formTwoChangeListenerMock, true, TEST_DATA_TYPE);
    formTwo.start();

    desktop.dataChanged(TEST_DATA_TYPE);
    Mockito.verify(formOneChangeListenerMock).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));
    Mockito.verify(formTwoChangeListenerMock).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));

    formOne.activate();
    Mockito.verify(formOneChangeListenerMock, Mockito.times(2)).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));
    Mockito.verify(formTwoChangeListenerMock).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));

    desktop.dataChanged(TEST_DATA_TYPE);
    Mockito.verify(formOneChangeListenerMock, Mockito.times(3)).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));
    Mockito.verify(formTwoChangeListenerMock).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));

    formTwo.activate();
    Mockito.verify(formOneChangeListenerMock, Mockito.times(3)).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));
    Mockito.verify(formTwoChangeListenerMock, Mockito.times(2)).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));

    formTwo.doClose();

    desktop.dataChanged(TEST_DATA_TYPE);
    Mockito.verify(formOneChangeListenerMock, Mockito.times(3)).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));
    Mockito.verify(formTwoChangeListenerMock, Mockito.times(2)).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));

    formOne.activate();
    Mockito.verify(formOneChangeListenerMock, Mockito.times(4)).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));
    Mockito.verify(formTwoChangeListenerMock, Mockito.times(2)).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));

    formOne.doClose();
    desktop.dataChanged(TEST_DATA_TYPE);
    Mockito.verify(formOneChangeListenerMock, Mockito.times(4)).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));
    Mockito.verify(formTwoChangeListenerMock, Mockito.times(2)).dataChanged(ArgumentMatchers.argThat(e -> e.getDataType().equals(TEST_DATA_TYPE)));

    // ensure all managers are removed
    assertTrue(BEANS.get(ActiveFormDataChangeManager.class).getDataChangeManagers().isEmpty());
  }

  @ClassId("0e6769a8-81c4-4292-8e3c-1abb6ebddec4")
  public static class TestForm extends AbstractForm {

    @Order(10)
    @ClassId("367f05c7-cecc-4392-9595-9134cc7a31c0")
    public class MainBox extends AbstractGroupBox {

    }
  }
}
