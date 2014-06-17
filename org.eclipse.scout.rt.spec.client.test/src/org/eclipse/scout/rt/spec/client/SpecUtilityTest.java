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
package org.eclipse.scout.rt.spec.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.annotations.Doc.Filtering;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.extension.client.ui.action.menu.AbstractExtensibleMenu;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopExtensibleMenu1;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopExtensibleMenu1.SubMenu1;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopExtensibleMenu2;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopExtensibleMenu2.SubExtensibleMenu;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopExtensibleMenu2.SubExtensibleMenu.SubSubMenu;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopExtensibleMenu2.SubMenu2;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopMenu1;
import org.eclipse.scout.rt.spec.client.filter.FilterUtility;
import org.eclipse.scout.rt.spec.client.filter.IDocFilter;
import org.eclipse.scout.rt.spec.client.utility.SpecUtility;
import org.junit.Test;

/**
 * Tests for {@link SpecUtility}
 */
public class SpecUtilityTest {

  @Test
  public void testExpandMenuHierarchy() throws ProcessingException {
    List<IMenu> menus = SpecUtility.expandMenuHierarchy(new TestForm().getFieldByClass(SmartField.class).getMenus());
    assertEquals(7, menus.size());
    assertTrue(menus.get(0) instanceof TopMenu1);
    assertTrue(menus.get(1) instanceof TopExtensibleMenu1);
    assertTrue(menus.get(2) instanceof SubMenu1);
    assertTrue(menus.get(3) instanceof TopExtensibleMenu2);
    assertTrue(menus.get(4) instanceof SubMenu2);
    assertTrue(menus.get(5) instanceof SubExtensibleMenu);
    assertTrue(menus.get(6) instanceof SubSubMenu);
  }

  /**
   * Test for {@link FilterUtility#isAccepted(Object, List)} considering only a field
   */
  @Test
  public void testIsAccepted() {
    testFilterAcceptance(Filtering.REJECT, false);
    testFilterAcceptance(Filtering.TRANSPARENT, false);
    testFilterAcceptance(Filtering.ACCEPT, true);
    testFilterAcceptance(Filtering.ACCEPT_REJECT_CHILDREN, true);
  }

  private void testFilterAcceptance(Filtering filtering, boolean expectAccepted) {
    IFormField testField = mock(AbstractFormField.class);
    when(testField.getLabel()).thenReturn(filtering.name());
    List<IDocFilter<IFormField>> filters = getParseLabelTestFilters();
    boolean accepted = FilterUtility.isAccepted(testField, filters);
    assertEquals("expected isAccepted() to return " + expectAccepted + " for " + filtering.name(), expectAccepted, accepted);
  }

  private List<IDocFilter<IFormField>> getParseLabelTestFilters() {
    List<IDocFilter<IFormField>> filters = new ArrayList<IDocFilter<IFormField>>();
    IDocFilter<IFormField> testFilter = new IDocFilter<IFormField>() {
      @Override
      public Filtering accept(IFormField field) {
        String label = field.getLabel();
        if (Filtering.ACCEPT.name().equals(label)) {
          return Filtering.ACCEPT;
        }
        if (Filtering.REJECT.name().equals(label)) {
          return Filtering.REJECT;
        }
        if (Filtering.TRANSPARENT.name().equals(label)) {
          return Filtering.TRANSPARENT;
        }
        if (Filtering.ACCEPT_REJECT_CHILDREN.name().equals(label)) {
          return Filtering.ACCEPT_REJECT_CHILDREN;
        }
        return Filtering.ACCEPT;
      }
    };

    filters.add(testFilter);
    return filters;
  }

  /**
   * Test for {@link FilterUtility#isAccepted(Object, List)} considering a field in a hierachy with various filtering
   * options for the GroupBox1 and the Field (Filtering for GroupBox2 is always Default=ACCEPTED
   * <p>
   * GroupBox1<br>
   * --> GroupBox2<br>
   * --> -->Field<br>
   */
  @Test
  public void testIsAcceptedHierarchic() {
    testFilterAcceptanceHierarchic(Filtering.REJECT, Filtering.REJECT, false);
    testFilterAcceptanceHierarchic(Filtering.TRANSPARENT, Filtering.REJECT, false);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT, Filtering.REJECT, false);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT_REJECT_CHILDREN, Filtering.REJECT, false);

    testFilterAcceptanceHierarchic(Filtering.REJECT, Filtering.TRANSPARENT, false);
    testFilterAcceptanceHierarchic(Filtering.TRANSPARENT, Filtering.TRANSPARENT, false);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT, Filtering.TRANSPARENT, true);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT_REJECT_CHILDREN, Filtering.TRANSPARENT, true);

    testFilterAcceptanceHierarchic(Filtering.REJECT, Filtering.ACCEPT, false);
    testFilterAcceptanceHierarchic(Filtering.TRANSPARENT, Filtering.ACCEPT, false);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT, Filtering.ACCEPT, true);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT_REJECT_CHILDREN, Filtering.ACCEPT, true);

    testFilterAcceptanceHierarchic(Filtering.REJECT, Filtering.ACCEPT_REJECT_CHILDREN, false);
    testFilterAcceptanceHierarchic(Filtering.TRANSPARENT, Filtering.ACCEPT_REJECT_CHILDREN, false);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT, Filtering.ACCEPT_REJECT_CHILDREN, false);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT_REJECT_CHILDREN, Filtering.ACCEPT_REJECT_CHILDREN, false);
  }

  private void testFilterAcceptanceHierarchic(Filtering filteringField, Filtering filteringParentParent, boolean expectAccepted) {
    AbstractGroupBox superSuperField = mock(AbstractGroupBox.class);
    when(superSuperField.getLabel()).thenReturn(filteringParentParent.name());

    AbstractGroupBox superField = mock(AbstractGroupBox.class);
    when(superField.getParentField()).thenReturn(superSuperField);

    IFormField testField = mock(AbstractFormField.class);
    when(testField.getParentField()).thenReturn(superField);
    when(testField.getLabel()).thenReturn(filteringField.name());

    List<IDocFilter<IFormField>> filters = getParseLabelTestFilters();
    assertEquals("expected isAccepted() to return " + expectAccepted, expectAccepted, FilterUtility.isAccepted(testField, filters));
  }

  /**
   * Test for {@link FilterUtility#isAccepted(Object, List)} without filters
   */
  @Test
  public void testAcceptedNoFilters() {
    IFormField testField = new AbstractFormField() {
    };
    boolean accepted = FilterUtility.isAccepted(testField, null);
    assertTrue(accepted);
  }

  class TestForm extends AbstractForm {

    public TestForm() throws ProcessingException {
      super();
    }

    public class MainBox extends AbstractGroupBox {
      @Order(10.0)
      public class SmartField extends AbstractSmartField<Long> {
        @Order(10.0)
        public class TopMenu1 extends AbstractMenu {
        }

        @Order(20.0)
        public class TopExtensibleMenu1 extends AbstractExtensibleMenu {
          @Order(90.0)
          public class SubMenu1 extends AbstractMenu {
          }
        }

        @Order(30.0)
        public class TopExtensibleMenu2 extends AbstractExtensibleMenu {
          @Order(10.0)
          public class SubMenu2 extends AbstractMenu {
          }

          @Order(20.0)
          public class SubExtensibleMenu extends AbstractExtensibleMenu {
            @Order(10.0)
            public class SubSubMenu extends AbstractMenu {
            }
          }
        }
      }
    }
  }

}
