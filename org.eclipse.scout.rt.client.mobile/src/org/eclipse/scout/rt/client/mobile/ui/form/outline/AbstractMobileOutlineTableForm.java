package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import java.util.List;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;

public abstract class AbstractMobileOutlineTableForm extends AbstractForm implements IOutlineTableForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractMobileOutlineTableForm.class);
  private List<IButton> m_mainboxButtons;

  public AbstractMobileOutlineTableForm(List<IButton> mainboxButtons) throws ProcessingException {
    super(false);
    m_mainboxButtons = mainboxButtons;
    callInitializer();
  }

  @Override
  protected boolean getConfiguredAskIfNeedSave() {
    return false;
  }

  @Override
  protected int getConfiguredDisplayHint() {
    return DISPLAY_HINT_VIEW;
  }

  @Override
  protected String getConfiguredDisplayViewId() {
    return VIEW_ID_PAGE_TABLE;
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public void start() throws ProcessingException {
    startInternal(new ViewHandler());
  }

  public void startAndLinkWithDesktop() throws ProcessingException {
    setAutoAddRemoveOnDesktop(false);
    start();
    ClientSyncJob.getCurrentSession().getDesktop().setOutlineTableForm(this);
  }

  @Order(10.0f)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected boolean getConfiguredBorderVisible() {
      return false;
    }

    @Override
    protected int getConfiguredGridColumnCount() {
      return 1;
    }

    @Override
    protected void injectFieldsInternal(List<IFormField> fieldList) {
      if (m_mainboxButtons != null) {
        fieldList.addAll(m_mainboxButtons);
      }

      super.injectFieldsInternal(fieldList);
    }
  }

  @Order(10.0f)
  public class ViewHandler extends AbstractFormHandler {
  }
}
