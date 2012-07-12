package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import java.util.List;

import org.eclipse.scout.commons.annotations.InjectFieldTo;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;

public class MobileOutlineTableForm extends AbstractMobileOutlineTableForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MobileOutlineTableForm.class);
  private List<IButton> m_mainboxButtons;

  public MobileOutlineTableForm(List<IButton> mainboxButtons) throws ProcessingException {
    super(mainboxButtons);
  }

  @Override
  public ITable getCurrentTable() {
    return getOutlineTableField().getTable();
  }

  @Override
  public void setCurrentTable(ITable table) {
    getOutlineTableField().installTable(table);
  }

  public OutlineTableField getOutlineTableField() {
    return getFieldByClass(OutlineTableField.class);
  }

  @InjectFieldTo(AbstractMobileOutlineTableForm.MainBox.class)
  @Order(10.0f)
  public class OutlineTableField extends AbstractMobileOutlineTableField {

  }

}
