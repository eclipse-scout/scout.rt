package org.eclipse.scout.rt.shared.extension.dto;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MainBoxPropertyExtension;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.PropertyExtensionData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.WrappedFormFieldOuterForm;
import org.eclipse.scout.rt.shared.extension.dto.fixture.WrappedFormFieldOuterFormData;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 5.2
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class WrappedFormExtensionTest extends AbstractLocalExtensionTestCase {

  private static final Long TEST_LONG = 42L;
  private static final String TEST_STRING = "test";

  @Test
  public void testExportOuterForm() {
    BEANS.get(IExtensionRegistry.class).register(MainBoxPropertyExtension.class, MainBox.class);
    BEANS.get(IExtensionRegistry.class).register(PropertyExtensionData.class, OrigFormData.class);

    OrigForm innerForm = new OrigForm();
    innerForm.initForm();
    innerForm.getMainBox().getExtension(MainBoxPropertyExtension.class).setLongValue(TEST_LONG);

    WrappedFormFieldOuterForm outerForm = new WrappedFormFieldOuterForm();
    outerForm.getWrappedFormField().setInnerForm(innerForm);
    outerForm.initForm();
    outerForm.getStringField().setValue(TEST_STRING);

    WrappedFormFieldOuterFormData formData = new WrappedFormFieldOuterFormData();
    outerForm.exportFormData(formData);

    assertEquals(TEST_STRING, formData.getString().getValue());
    assertEquals(TEST_LONG, innerForm.getMainBox().getExtension(MainBoxPropertyExtension.class).getLongValue());
  }

  @Test
  public void testImportOuterForm() {
    BEANS.get(IExtensionRegistry.class).register(MainBoxPropertyExtension.class, MainBox.class);
    BEANS.get(IExtensionRegistry.class).register(PropertyExtensionData.class, OrigFormData.class);

    OrigForm innerForm = new OrigForm();
    innerForm.initForm();
    innerForm.getFirstStringField().setValue(TEST_STRING);
    innerForm.getMainBox().getExtension(MainBoxPropertyExtension.class).setLongValue(TEST_LONG);

    WrappedFormFieldOuterForm outerForm = new WrappedFormFieldOuterForm();
    outerForm.getWrappedFormField().setInnerForm(innerForm);
    outerForm.initForm();

    WrappedFormFieldOuterFormData formData = new WrappedFormFieldOuterFormData();
    formData.getString().setValue(TEST_STRING);
    outerForm.importFormData(formData);

    assertEquals(TEST_STRING, formData.getString().getValue());
    assertEquals(TEST_LONG, innerForm.getMainBox().getExtension(MainBoxPropertyExtension.class).getLongValue());
  }
}
