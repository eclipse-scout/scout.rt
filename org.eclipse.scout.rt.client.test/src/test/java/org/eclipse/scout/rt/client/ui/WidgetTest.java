/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.BottomBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.TopBox.NameField;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.WidgetTest.Desktop.ViewButton;
import org.eclipse.scout.rt.client.ui.WidgetTest.NestedForm.MainBox.GroupBox;
import org.eclipse.scout.rt.client.ui.WidgetTest.NestedForm.MainBox.GroupBox.NestedField;
import org.eclipse.scout.rt.client.ui.WidgetTest.ReplacedDesktop.ReplacedViewButton;
import org.eclipse.scout.rt.client.ui.WidgetTest.ReplacedFieldForm.ReplacedSimpleField;
import org.eclipse.scout.rt.client.ui.WidgetTest.ReplacedNestedForm.ReplacedNestedField;
import org.eclipse.scout.rt.client.ui.WidgetTest.SiblingExtensionForm.MainBox.NameExtField;
import org.eclipse.scout.rt.client.ui.WidgetTest.SiblingExtensionForm.MainBox.NameSuperField;
import org.eclipse.scout.rt.client.ui.WidgetTest.SiblingExtensionForm.MainBox.PreNameExtField;
import org.eclipse.scout.rt.client.ui.WidgetTest.SiblingExtensionForm.MainBox.PreNameSuperField;
import org.eclipse.scout.rt.client.ui.WidgetTest.SimpleForm.MainBox.SimpleField;
import org.eclipse.scout.rt.client.ui.WidgetTest.WidgetWithChildren.ChildWidget;
import org.eclipse.scout.rt.client.ui.WidgetTest.WidgetWithChildren.ChildWidget.ChildChildWidget;
import org.eclipse.scout.rt.client.ui.WidgetTest.WidgetWithChildren.ChildWidget2;
import org.eclipse.scout.rt.client.ui.action.view.AbstractViewButton;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class WidgetTest {

  @Test
  public void testInitConfig() {
    Widget widget = new Widget();
    assertEquals(1, widget.initConfigCalls);
    assertEquals(true, widget.isInitConfigDone());
  }

  @Test
  public void testInit() {
    Widget widget = new Widget();
    assertEquals(0, widget.initCalls);
    assertEquals(false, widget.isInitDone());

    widget.init();
    assertEquals(1, widget.initCalls);
    assertEquals(true, widget.isInitDone());

    // Does not execute init again
    widget.init();
    assertEquals(1, widget.initCalls);
    assertEquals(true, widget.isInitDone());
  }

  @Test
  public void testDispose() {
    Widget widget = new Widget();
    assertEquals(0, widget.disposeCalls);
    assertEquals(false, widget.isDisposeDone());

    widget.init();
    assertEquals(0, widget.disposeCalls);
    assertEquals(false, widget.isDisposeDone());

    widget.dispose();
    assertEquals(1, widget.disposeCalls);
    assertEquals(true, widget.isDisposeDone());

    // Does not execute dispose again
    widget.dispose();
    assertEquals(1, widget.disposeCalls);
    assertEquals(true, widget.isDisposeDone());
  }

  @Test
  public void testInitAfterDispose() {
    Widget widget = new Widget();
    assertEquals(0, widget.disposeCalls);
    assertEquals(false, widget.isDisposeDone());

    widget.init();
    assertEquals(0, widget.disposeCalls);
    assertEquals(false, widget.isDisposeDone());

    widget.dispose();
    assertEquals(1, widget.disposeCalls);
    assertEquals(false, widget.isInitDone());
    assertEquals(true, widget.isDisposeDone());

    // Init may be called again after dispose
    // The reason is: it has always been like this for forms and we don't want to break existing code
    widget.init();
    assertEquals(2, widget.initCalls);
    assertEquals(true, widget.isInitDone());
    assertEquals(false, widget.isDisposeDone());

    widget.dispose();
    assertEquals(2, widget.disposeCalls);
    assertEquals(false, widget.isInitDone());
    assertEquals(true, widget.isDisposeDone());
  }

  @Test
  public void testAddCssClass() {
    IWidget widget = new Widget();
    widget.addCssClass("custom-class");
    assertEquals("custom-class", widget.getCssClass());

    widget.addCssClass("another-class1 another-class2");
    assertEquals("custom-class another-class1 another-class2", widget.getCssClass());

    // Does not add the same class twice
    widget.addCssClass("another-class1");
    assertEquals("custom-class another"
        + "-class1 another-class2", widget.getCssClass());

    widget.addCssClass("another-class2 another-class1");
    assertEquals("custom-class another-class1 another-class2", widget.getCssClass());
  }

  @Test
  public void testRemoveCssClass() {
    IWidget widget = new Widget();
    widget.setCssClass("cls1 cls2 cls3");
    assertEquals("cls1 cls2 cls3", widget.getCssClass());

    widget.removeCssClass("cls2");
    assertEquals("cls1 cls3", widget.getCssClass());

    widget.removeCssClass("cls3 cls1");
    assertEquals("", widget.getCssClass());
  }

  @Test
  public void testGetWidgetByClass() {
    IWidget widget = new WidgetWithChildren();
    assertEquals(ChildWidget.class, widget.getWidgetByClass(ChildWidget.class).getClass());
    assertEquals(ChildWidget2.class, widget.getWidgetByClass(ChildWidget2.class).getClass());
    assertEquals(ChildChildWidget.class, widget.getWidgetByClass(ChildChildWidget.class).getClass());
  }

  @Test
  public void testGetWidgetByClass_replacedWidget() {
    IDesktop desktop = new Desktop();
    assertEquals(ViewButton.class, desktop.getWidgetByClass(ViewButton.class).getClass());
    assertEquals(null, desktop.getWidgetByClass(ReplacedViewButton.class));

    IDesktop replacedDesktop = new ReplacedDesktop();
    assertEquals(ReplacedViewButton.class, replacedDesktop.getWidgetByClass(ViewButton.class).getClass());
    assertEquals(ReplacedViewButton.class, replacedDesktop.getWidgetByClass(ReplacedViewButton.class).getClass());
  }

  @Test
  public void testGetWidgetByClass_movedField() {
    BEANS.get(IExtensionRegistry.class).registerMove(NameField.class, 20d, BottomBox.class);
    OrigForm form = new OrigForm();
    assertEquals(1, form.getTopBox().getFieldCount());
    assertEquals(2, form.getBottomBox().getFieldCount());
    assertEquals(form.getNameField().getClass(), NameField.class);
    assertSame(form.getNameField(), form.getBottomBox().getFields().get(1));
    assertEquals(NameField.class, form.getWidgetByClass(NameField.class).getClass());
  }

  @Test
  public void testGetWidgetByClass_replacedField() {
    ReplacedFieldForm form = new ReplacedFieldForm();
    assertEquals(ReplacedSimpleField.class, form.getFieldByClass(SimpleField.class).getClass());
    assertEquals(ReplacedSimpleField.class, form.getFieldByClass(ReplacedSimpleField.class).getClass());

    // getWidgetByClass must do the same as getFieldByClass
    assertEquals(ReplacedSimpleField.class, form.getWidgetByClass(SimpleField.class).getClass());
    assertEquals(ReplacedSimpleField.class, form.getWidgetByClass(ReplacedSimpleField.class).getClass());
  }

  @Test
  public void testGetWidgetByClass_replacedFieldNested() {
    NestedForm form = new ReplacedNestedForm();
    assertEquals(ReplacedNestedField.class, form.getFieldByClass(NestedField.class).getClass());
    assertEquals(ReplacedNestedField.class, form.getFieldByClass(ReplacedNestedField.class).getClass());

    // getWidgetByClass must do the same as getFieldByClass
    assertEquals(ReplacedNestedField.class, form.getWidgetByClass(NestedField.class).getClass());
    assertEquals(ReplacedNestedField.class, form.getWidgetByClass(ReplacedNestedField.class).getClass());
  }

  @Test
  public void testGetWidgetByClass_siblingExtension() {
    SiblingExtensionForm form = new SiblingExtensionForm();
    assertEquals(NameSuperField.class, form.getFieldByClass(NameSuperField.class).getClass());
    assertEquals(NameExtField.class, form.getFieldByClass(NameExtField.class).getClass());
    assertEquals(PreNameSuperField.class, form.getFieldByClass(PreNameSuperField.class).getClass());
    assertEquals(PreNameExtField.class, form.getFieldByClass(PreNameExtField.class).getClass());

    // getWidgetByClass must do the same as getFieldByClass
    assertEquals(NameSuperField.class, form.getWidgetByClass(NameSuperField.class).getClass());
    assertEquals(NameExtField.class, form.getWidgetByClass(NameExtField.class).getClass());
    assertEquals(PreNameSuperField.class, form.getWidgetByClass(PreNameSuperField.class).getClass());
    assertEquals(PreNameExtField.class, form.getWidgetByClass(PreNameExtField.class).getClass());
  }

  protected class Widget extends AbstractWidget {

    public int initCalls = 0;
    public int initConfigCalls;
    public int disposeCalls = 0;

    @Override
    protected void initConfigInternal() {
      super.initConfigInternal();
      initConfigCalls++;
    }

    @Override
    protected void initInternal() {
      super.initInternal();
      initCalls++;
    }

    @Override
    protected void disposeInternal() {
      disposeCalls++;
      super.disposeInternal();
    }
  }

  protected static class WidgetWithChildren extends AbstractWidget {

    @Override
    public List<? extends IWidget> getChildren() {
      List<IWidget> result = new ArrayList<>();
      result.add(new ChildWidget());
      result.add(new ChildWidget2());
      return result;
    }

    public class ChildWidget extends AbstractWidget {
      @Override
      public List<? extends IWidget> getChildren() {
        List<IWidget> result = new ArrayList<>();
        result.add(new ChildChildWidget());
        return result;
      }

      public class ChildChildWidget extends AbstractWidget {

      }
    }

    public class ChildWidget2 extends AbstractWidget {
    }
  }

  protected static class Desktop extends AbstractDesktop {
    public class ViewButton extends AbstractViewButton {

    }
  }

  protected static class ReplacedDesktop extends Desktop {

    @Replace
    public class ReplacedViewButton extends ViewButton {

    }
  }

  protected static class SiblingExtensionForm extends AbstractForm {

    @Order(5)
    public class MainBox extends AbstractGroupBox {

      @Order(5)
      public class NameSuperField extends AbstractStringField {
      }

      @Order(10)
      public class NameExtField extends NameSuperField {
      }

      @Order(15)
      public class PreNameExtField extends PreNameSuperField {
      }

      @Order(20)
      public class PreNameSuperField extends AbstractStringField {
      }
    }
  }

  protected static class SimpleForm extends AbstractForm {
    @Order(5)
    public class MainBox extends AbstractGroupBox {
      @Order(5)
      public class SimpleField extends AbstractStringField {
      }
    }
  }

  protected static class ReplacedFieldForm extends SimpleForm {

    @Replace
    public class ReplacedSimpleField extends SimpleField {
      public ReplacedSimpleField(MainBox container) {
        container.super();
      }
    }
  }

  protected static class NestedForm extends AbstractForm {
    @Order(5)
    public class MainBox extends AbstractGroupBox {
      @Order(5)
      public class GroupBox extends AbstractGroupBox {
        @Order(5)
        public class NestedField extends AbstractStringField {
        }
      }
    }
  }

  protected static class ReplacedNestedForm extends NestedForm {

    @Replace
    public class ReplacedNestedField extends NestedField {
      public ReplacedNestedField(GroupBox container) {
        container.super();
      }
    }
  }
}
