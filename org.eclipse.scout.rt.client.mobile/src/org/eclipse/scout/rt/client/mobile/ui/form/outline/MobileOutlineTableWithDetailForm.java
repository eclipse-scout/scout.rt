package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.commons.annotations.InjectFieldTo;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.ui.desktop.ActiveOutlineObserver;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.MobileOutlineTableWithDetailForm.GroupBox.OutlineTableField;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;

public class MobileOutlineTableWithDetailForm extends AbstractMobileOutlineTableForm implements IOutlineTableForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MobileOutlineTableWithDetailForm.class);
  private P_OutlinePropertyChangeListener m_otlinePropertyChangeListener;
  private ActiveOutlineObserver m_activeOutlineObserver;

  public MobileOutlineTableWithDetailForm(List<IButton> mainboxButtons) throws ProcessingException {
    super(mainboxButtons);
    m_activeOutlineObserver = new ActiveOutlineObserver();
    m_otlinePropertyChangeListener = new P_OutlinePropertyChangeListener();
    m_activeOutlineObserver.addOutlinePropertyChangeListener(m_otlinePropertyChangeListener);
  }

  @Override
  public ITable getCurrentTable() {
    return getOutlineTableField().getTable();
  }

  @Override
  public void setCurrentTable(ITable table) {
    getOutlineTableField().installTable(table);

    boolean tableVisible = true;
    if (table == null) {
      if (m_activeOutlineObserver.getActiveOutline() != null && m_activeOutlineObserver.getActiveOutline().getActivePage() != null) {
        tableVisible &= !m_activeOutlineObserver.getActiveOutline().getActivePage().isLeaf();
      }
    }
    getOutlineTableField().setVisible(tableVisible);
  }

  public OutlineTableField getOutlineTableField() {
    return getFieldByClass(OutlineTableField.class);
  }

  public WrappedPageDetailForm getWrappedPageDetailForm() {
    return getFieldByClass(WrappedPageDetailForm.class);
  }

  @InjectFieldTo(AbstractMobileOutlineTableForm.MainBox.class)
  @Order(5.0f)
  public class WrappedPageDetailForm extends AbstractWrappedFormField<IForm> {

    @Override
    protected int getConfiguredGridW() {
      return 2;
    }

    @Override
    protected int getConfiguredGridH() {
      return 2;
    }

  }

  @InjectFieldTo(AbstractMobileOutlineTableForm.MainBox.class)
  @Order(10.0f)
  public class GroupBox extends AbstractGroupBox {

    @Order(10.0f)
    public class OutlineTableField extends AbstractMobileOutlineTableField {

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }

    }

  }

  public void setDetailForm(IForm detailForm) {
    getWrappedPageDetailForm().setInnerForm(detailForm);
    getWrappedPageDetailForm().setVisible(detailForm != null);
  }

  private class P_OutlinePropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals(IOutline.PROP_DETAIL_FORM)) {
        IForm detailForm = (IForm) e.getNewValue();
        setDetailForm(detailForm);
      }
    }
  }
}
