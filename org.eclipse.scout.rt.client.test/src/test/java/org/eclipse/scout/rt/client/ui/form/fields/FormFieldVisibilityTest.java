package org.eclipse.scout.rt.client.ui.form.fields;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.FormFieldVisibilityTest.P_GroupBox.P_RadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.FormFieldVisibilityTest.P_GroupBox.P_RadioButtonGroup.P_Button1;
import org.eclipse.scout.rt.client.ui.form.fields.FormFieldVisibilityTest.P_GroupBox.P_RadioButtonGroup.P_Button2;
import org.eclipse.scout.rt.client.ui.form.fields.FormFieldVisibilityTest.P_GroupBox.P_TabBox.Tab1;
import org.eclipse.scout.rt.client.ui.form.fields.FormFieldVisibilityTest.P_GroupBox.P_TabBox.Tab1.P_TreeBox;
import org.eclipse.scout.rt.client.ui.form.fields.FormFieldVisibilityTest.P_GroupBox.P_TabBox.Tab1.P_TreeBox.P_Seq;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link FormFieldVisibilityTest}</h3>
 */
public class FormFieldVisibilityTest {

  @Test
  public void testVisitFields() {
    ICompositeField field = createFixture();
    final AtomicInteger counter = new AtomicInteger(0);
    field.visitFields(new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField f, int level, int fieldIndex) {
        if (f.getClass().getName().startsWith(FormFieldVisibilityTest.class.getName())) {
          // ignore the filter boxes of treebox and listbox
          counter.incrementAndGet();
        }
        return true;
      }
    });
    Assert.assertEquals(12, counter.intValue());
  }

  @Test
  public void testVisitParents() {
    ICompositeField root = createFixture();
    final AtomicInteger counter01 = new AtomicInteger(0);
    root.getFieldByClass(P_Seq.class).visitParents(new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField f, int level, int fieldIndex) {
        counter01.incrementAndGet();
        return true;
      }
    });
    Assert.assertEquals(4, counter01.intValue());

    final AtomicInteger counter02 = new AtomicInteger(0);
    root.getFieldByClass(P_Button2.class).visitParents(new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField f, int level, int fieldIndex) {
        counter02.incrementAndGet();
        return true;
      }
    });
    Assert.assertEquals(2, counter02.intValue());

    final AtomicInteger counter03 = new AtomicInteger(0);
    root.visitParents(new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField f, int level, int fieldIndex) {
        counter03.incrementAndGet();
        return true;
      }
    });
    Assert.assertEquals(0, counter03.intValue());

    final AtomicInteger counter04 = new AtomicInteger(0);
    final Holder<Object> lastVisited = new Holder<>(Object.class);
    root.getFieldByClass(P_Seq.class).visitParents(new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField f, int level, int fieldIndex) {
        counter04.incrementAndGet();
        lastVisited.setValue(f);
        return counter04.intValue() <= 1;
      }
    });
    Assert.assertEquals(2, counter04.intValue());
    Assert.assertSame(lastVisited.getValue(), root.getFieldByClass(Tab1.class));
  }

  private ICompositeField createFixture() {
    ICompositeField result = new P_GroupBox();

    // verify structure
    Assert.assertEquals(2, result.getFieldCount());
    Assert.assertTrue(result.getFields().get(0) instanceof IRadioButtonGroup<?>);
    Assert.assertEquals(2, ((IRadioButtonGroup<?>) result.getFields().get(0)).getFieldCount());

    Assert.assertTrue(result.getFields().get(1) instanceof ITabBox);
    ITabBox tabbox = (ITabBox) result.getFields().get(1);
    Assert.assertEquals(2, tabbox.getFieldCount());

    IGroupBox tab1 = (IGroupBox) tabbox.getFields().get(0);
    IGroupBox tab2 = (IGroupBox) tabbox.getFields().get(1);
    Assert.assertEquals(1, tab1.getFieldCount());
    Assert.assertEquals(1, tab2.getFieldCount());

    ITreeBox<?> treebox = (ITreeBox<?>) tab1.getFields().get(0);
    IListBox<?> listbox = (IListBox<?>) tab2.getFields().get(0);
    Assert.assertEquals(2 /*treebox filter box is included as well*/, treebox.getFieldCount());
    Assert.assertEquals(2 /*listbox filter box is included as well*/, listbox.getFieldCount());

    return result;
  }

  @Test
  public void testGetConfigured() {
    ICompositeField field = createFixture();
    Assert.assertTrue(field.isVisible());
    Assert.assertTrue(field.isVisibleGranted());
    Assert.assertFalse(field.getFieldByClass(P_Button2.class).isVisible());
    Assert.assertFalse(field.getFieldByClass(P_Button2.class).isVisibleIncludingParents());
    Assert.assertTrue(field.getFieldByClass(P_Button2.class).isVisibleGranted());

    Assert.assertFalse(field.getFieldByClass(Tab1.class).isVisible());
    Assert.assertTrue(field.getFieldByClass(Tab1.class).isVisibleGranted());

    Assert.assertTrue(field.getFieldByClass(P_TreeBox.class).isVisible());
    Assert.assertTrue(field.getFieldByClass(P_TreeBox.class).isVisibleGranted());
    Assert.assertFalse(field.getFieldByClass(P_TreeBox.class).isVisibleIncludingParents());

    Assert.assertTrue(field.getFieldByClass(P_Seq.class).isVisible());
    Assert.assertTrue(field.getFieldByClass(P_Seq.class).isVisibleGranted());
    Assert.assertFalse(field.getFieldByClass(P_Seq.class).isVisibleIncludingParents());
  }

  @Test
  public void testVisibleInheritance() {
    ICompositeField field = createFixture();
    field.getFieldByClass(P_RadioButtonGroup.class).setVisibleGranted(false);
    field.getFieldByClass(P_RadioButtonGroup.class).setVisible(true);

    Assert.assertTrue(field.isVisible());
    Assert.assertTrue(field.isVisibleGranted());
    Assert.assertFalse(field.getFieldByClass(P_RadioButtonGroup.class).isVisibleGranted());
    Assert.assertTrue(field.getFieldByClass(P_RadioButtonGroup.class).isVisible(IDimensions.VISIBLE));
    Assert.assertFalse(field.getFieldByClass(P_RadioButtonGroup.class).isVisible());
    Assert.assertFalse(field.getFieldByClass(P_RadioButtonGroup.class).isVisibleIncludingParents());

    Assert.assertTrue(field.getFieldByClass(P_Button1.class).isVisible());
    Assert.assertFalse(field.getFieldByClass(P_Button1.class).isVisibleIncludingParents());
    field.getFieldByClass(P_Button1.class).setVisible(false);

    Assert.assertFalse(field.getFieldByClass(P_Button2.class).isVisible());
    Assert.assertFalse(field.getFieldByClass(P_Button2.class).isVisibleIncludingParents());

    field.getFieldByClass(P_Button2.class).setVisible(true);
    Assert.assertTrue(field.getFieldByClass(P_Button2.class).isVisible());
    Assert.assertFalse(field.getFieldByClass(P_Button2.class).isVisibleIncludingParents());

    field.getFieldByClass(P_Button2.class).setVisibleGranted(true, true);
    Assert.assertTrue(field.getFieldByClass(P_Button2.class).isVisible());
    Assert.assertTrue(field.getFieldByClass(P_Button2.class).isVisibleIncludingParents());
    Assert.assertTrue(field.getFieldByClass(P_RadioButtonGroup.class).isVisibleGranted());
    Assert.assertTrue(field.getFieldByClass(P_RadioButtonGroup.class).isVisible());

    Assert.assertTrue(field.getFieldByClass(P_Button1.class).isVisibleGranted());
    Assert.assertFalse(field.getFieldByClass(P_Button1.class).isVisible());
    Assert.assertFalse(field.getFieldByClass(P_Button1.class).isVisibleIncludingParents());
    field.getFieldByClass(P_Button1.class).setVisible(true);
    Assert.assertTrue(field.getFieldByClass(P_Button1.class).isVisibleGranted());
    Assert.assertTrue(field.getFieldByClass(P_Button1.class).isVisible());
    Assert.assertTrue(field.getFieldByClass(P_Button1.class).isVisibleIncludingParents());

    Assert.assertFalse(field.getFieldByClass(Tab1.class).isVisible());
    Assert.assertTrue(field.getFieldByClass(P_TreeBox.class).isVisible());
    Assert.assertFalse(field.getFieldByClass(P_TreeBox.class).isVisibleIncludingParents());
    field.getFieldByClass(P_TreeBox.class).setVisible(true, true);
    Assert.assertTrue(field.getFieldByClass(P_TreeBox.class).isVisible());
    Assert.assertTrue(field.getFieldByClass(P_TreeBox.class).isVisibleIncludingParents());
    Assert.assertTrue(field.getFieldByClass(Tab1.class).isVisible());
  }

  public static class P_GroupBox extends AbstractGroupBox {

    public class P_RadioButtonGroup extends AbstractRadioButtonGroup<Long> {
      public class P_Button1 extends AbstractRadioButton<Long> {
      }

      public class P_Button2 extends AbstractRadioButton<Long> {
        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }
      }
    }

    public class P_TabBox extends AbstractTabBox {
      public class Tab1 extends AbstractGroupBox {

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        public class P_TreeBox extends AbstractTreeBox<Long> {
          public class P_Seq extends AbstractSequenceBox {
            public class P_String extends AbstractStringField {
            }
          }
        }
      }

      public class Tab2 extends AbstractGroupBox {
        public class P_ListBox extends AbstractListBox {

          public class P_BigDec extends AbstractBigDecimalField {
          }
        }
      }
    }
  }
}
