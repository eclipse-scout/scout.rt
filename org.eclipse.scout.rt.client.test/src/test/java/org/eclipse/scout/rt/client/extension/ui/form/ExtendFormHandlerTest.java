package org.eclipse.scout.rt.client.extension.ui.form;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.FormWithHandler;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.FormWithHandlerExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.FormWithHandlerExtension.ModifyHandlerExtension;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 6.0
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ExtendFormHandlerTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testExtendFormHandler() {
    BEANS.get(IExtensionRegistry.class).register(FormWithHandlerExtension.class);

    FormWithHandler form = new FormWithHandler();
    assertNotNull(form.getExtension(FormWithHandlerExtension.class));

    form.setHandler(form.new ModifyHandler());
    AbstractFormHandler handler = (AbstractFormHandler) form.getHandler();
    ModifyHandlerExtension modifyHanlderExtension = handler.getExtension(ModifyHandlerExtension.class);
    assertNotNull(modifyHanlderExtension);

    assertFalse(modifyHanlderExtension.isLoaded());

    form.start();
    assertTrue(modifyHanlderExtension.isLoaded());
    form.doClose();
  }
}
