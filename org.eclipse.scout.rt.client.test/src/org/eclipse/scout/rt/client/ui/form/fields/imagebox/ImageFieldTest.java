package org.eclipse.scout.rt.client.ui.form.fields.imagebox;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.ImageFieldTest.TestForm.MainBox.ImageField;
import org.eclipse.scout.testing.client.form.FormHandler;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractImageField}
 * 
 * @since 3.10.0-M4
 */
@RunWith(ScoutClientTestRunner.class)
public class ImageFieldTest {

  public static class TestForm extends AbstractForm {

    public TestForm() throws ProcessingException {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return "Test Form";
    }

    public void startForm() throws ProcessingException {
      startInternal(new FormHandler());
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    public AbstractImageField getImageField() {
      return getFieldByClass(ImageField.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
      @Order(10.0)
      public class ImageField extends AbstractImageField {

        @Order(10.0)
        public class ImageFieldMenu extends AbstractMenu {

          @Override
          protected String getConfiguredKeyStroke() {
            return "control-a";
          }

          @Override
          protected String getConfiguredText() {
            return "&ImageFieldMenu";
          }
        }
      }
    }
  }

  private TestForm m_form;

  @Before
  public void setUp() throws Throwable {
    m_form = new TestForm();
    m_form.startForm();
  }

  @Test
  public void testMenusAndKeyStrokes() {
    IMenu[] imageFieldMenus = m_form.getImageField().getMenus();
    Assert.assertEquals("ImageField should have 1 menu", 1, imageFieldMenus.length);
    Assert.assertEquals("ImageFieldMenu", imageFieldMenus[0].getText());
    Assert.assertEquals("&ImageFieldMenu", imageFieldMenus[0].getTextWithMnemonic());
    Assert.assertEquals("control-a", imageFieldMenus[0].getKeyStroke());

    IKeyStroke[] imageFieldKeyStrokes = m_form.getImageField().getContributedKeyStrokes();
    Assert.assertNotNull("KeyStrokes of ImageField should not be null", imageFieldKeyStrokes);
    Assert.assertEquals("ImageField should have 1 keyStrokes registered", 1, imageFieldKeyStrokes.length);
  }

  @After
  public void tearDown() throws Throwable {
    m_form.doClose();
  }
}
