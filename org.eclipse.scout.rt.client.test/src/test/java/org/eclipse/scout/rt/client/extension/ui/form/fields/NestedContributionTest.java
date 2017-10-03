package org.eclipse.scout.rt.client.extension.ui.form.fields;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.fields.NestedContributionTest.Contribution1.FirstGroupBox;
import org.eclipse.scout.rt.client.extension.ui.form.fields.NestedContributionTest.Contribution2.SecondGroupBox;
import org.eclipse.scout.rt.client.extension.ui.form.fields.NestedContributionTest.MyForm.MainBox;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that nested contributions are found even if the contribution to find is not in the FIRST branch of nested
 * contributions
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class NestedContributionTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testNestedContribution() throws Exception {
    // find the contribution that will be at the second position in a HashMap
    final Map<Class, Boolean> findOrder = new HashMap<>(2);
    findOrder.put(FirstGroupBox.class, Boolean.TRUE);
    findOrder.put(SecondGroupBox.class, Boolean.FALSE);
    final Iterator<Class> iterator = findOrder.keySet().iterator();
    iterator.next(); // skip first;
    final Class second = iterator.next(); // will be the second element in the contribution map. The element to search (boolean field) must be a child contribution of this class.

    final IExtensionRegistry registry = BEANS.get(IExtensionRegistry.class);
    registry.register(Contribution1.class);
    registry.register(Contribution2.class);
    registry.register(BooleanField.class, second); // register the boolean field contribution into the second contribution (which is either FirstGroupBox or SecondGroupBox).

    final MyForm frm = new MyForm();
    final MainBox mainBox = frm.getMainBox();
    assertNotNull(mainBox.getContribution(FirstGroupBox.class));
    assertNotNull(mainBox.getContribution(SecondGroupBox.class));
    assertNotNull(mainBox.getContribution(BooleanField.class));
  }

  public static class MyForm extends AbstractForm {

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
      @Order(10)
      public class NameField extends AbstractStringField {
      }
    }
  }

  public static class Contribution1 extends AbstractGroupBoxExtension<NestedContributionTest.MyForm.MainBox> {
    public Contribution1(MainBox owner) {
      super(owner);
    }

    public class FirstGroupBox extends AbstractGroupBox {
    }
  }

  public static class Contribution2 extends AbstractGroupBoxExtension<NestedContributionTest.MyForm.MainBox> {
    public Contribution2(MainBox owner) {
      super(owner);
    }

    public class SecondGroupBox extends AbstractGroupBox {
    }
  }

  public static class BooleanField extends AbstractBooleanField {
  }
}
