package org.eclipse.scout.rt.client.ui.form.fields.treebox;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.TreeBoxTestForm.MainBox.FirstTreeBox;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.TreeBoxTestForm.MainBox.SecondTreeBox;

/**
 * @since 5.2
 */
@ClassId("64911272-1640-4a1d-95f9-d9257b773f5d")
public class TreeBoxTestForm extends AbstractForm {

  public static final String FIRST_TREE_BOX_CLASS_ID = "2b490d07-069a-4974-afd2-4c97e3b0e05c";
  public static final String SECOND_TREE_BOX_CLASS_ID = "e8a73e93-ee93-43a0-9659-9988e5f7ead6";

  public TreeBoxTestForm() throws ProcessingException {
    super();
  }

  public FirstTreeBox getFirstTreeBox() {
    return getFieldByClass(FirstTreeBox.class);
  }

  public SecondTreeBox getSecondTreeBox() {
    return getFieldByClass(SecondTreeBox.class);
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  @ClassId("8acccbdc-41c7-40b1-ae0b-c4c46682788a")
  public class MainBox extends AbstractGroupBox {

    @Order(1000)
    @ClassId(FIRST_TREE_BOX_CLASS_ID)
    public class FirstTreeBox extends AbstractTreeBox<Long> {
    }

    @Order(2000)
    @ClassId(SECOND_TREE_BOX_CLASS_ID)
    public class SecondTreeBox extends AbstractTreeBox<Long> {
    }
  }
}
