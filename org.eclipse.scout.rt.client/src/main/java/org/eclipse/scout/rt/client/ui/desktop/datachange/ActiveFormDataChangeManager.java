package org.eclipse.scout.rt.client.ui.desktop.datachange;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * Data change manager which may be used to avoid data-change notifications for hidden forms.
 * Events for listeners registered to this manager will be buffered until the form is activated.
 */
@ApplicationScoped
public class ActiveFormDataChangeManager implements IDataChangeListener {

  protected static final String DATA_CHANGE_MANAGERS_KEY = "activeFormDataChangeManagers";

  protected Map<IForm, IDataChangeManager> getDataChangeManagers() {
    IClientSession clientSession = ClientSessionProvider.currentSession();
    @SuppressWarnings("unchecked") Map<IForm, IDataChangeManager> dataChangeManagers =
        (Map<IForm, IDataChangeManager>) clientSession.getData(DATA_CHANGE_MANAGERS_KEY);
    if (dataChangeManagers == null) {
      dataChangeManagers = new HashMap<>();
      clientSession.setData(DATA_CHANGE_MANAGERS_KEY, dataChangeManagers);
      attachToDesktop(clientSession.getDesktop());
    }
    return dataChangeManagers;
  }

  protected void attachToDesktop(IDesktop desktop) {
    desktop.addPropertyChangeListener(IDesktop.PROP_IN_BACKGROUND, e -> onDesktopInBackground((boolean) e.getNewValue()));
    desktop.addDesktopListener(e -> onFormActivate(e.getForm()), DesktopEvent.TYPE_FORM_ACTIVATE);
    desktop.dataChangeListeners().add(this, true);
  }

  @Override
  public void dataChanged(DataChangeEvent event) {
    getDataChangeManagers().values().forEach(m -> m.fireEvent(event));
  }

  public void add(IForm form, IDataChangeListener listener, boolean weak, Object... dataTypes) {
    getDataChangeManagers()
        .computeIfAbsent(form, this::createDataChangeManager)
        .add(listener, weak, dataTypes);
  }

  public void remove(IDataChangeListener listener) {
    getDataChangeManagers().values().forEach(m -> m.remove(listener));
  }

  protected IDataChangeManager createDataChangeManager(IForm form) {
    form.addFormListener(e -> onFormClosed(e.getForm()), FormEvent.TYPE_CLOSED);
    return BEANS.get(IDataChangeManager.class);
  }

  protected void onFormActivate(IForm activatedForm) {
    getDataChangeManagers().forEach((f, m) -> m.setBuffering(f != activatedForm));
  }

  protected void onFormClosed(IForm closedForm) {
    getDataChangeManagers().remove(closedForm);
  }

  protected void onDesktopInBackground(boolean inBackground) {
    if (!inBackground) {
      getDataChangeManagers().values().forEach(m -> m.setBuffering(true));
    }
  }
}
