/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.workbench;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.CompositeLong;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.wizard.IWizard;
import org.eclipse.scout.rt.ui.rap.AbstractRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.basic.WidgetPrinter;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.workbench.window.editor.AbstractScoutEditorPart;
import org.eclipse.scout.rt.ui.rap.workbench.window.editor.ScoutFormEditorInput;
import org.eclipse.scout.rt.ui.rap.workbench.window.view.AbstractScoutView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;
import org.osgi.framework.Bundle;

/**
 * <h3>TestWorkbenchRwtEnvironment</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 31.03.2011
 * @deprecated will be removed with the M-release.
 */
@Deprecated
@SuppressWarnings("deprecation")
public abstract class AbstractRwtWorkbenchEnvironment extends AbstractRwtEnvironment implements IRwtWorkbenchEnvironment {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractRwtWorkbenchEnvironment.class);

  private Display m_display;
  private HashMap<String, String> m_scoutPartIdToUiPartId;
  private Map<String, List<IForm>> m_openLaterMap = new HashMap<String, List<IForm>>();
  private OptimisticLock m_activateViewLock;
  private P_PerspectiveListener m_perspectiveListener;
  private final String m_perspectiveId;

  public AbstractRwtWorkbenchEnvironment(Bundle applicationBundle, Class<? extends IClientSession> clientSessionClazz, String perspectiveId) {
    super(applicationBundle, clientSessionClazz);
    m_perspectiveId = perspectiveId;
    m_scoutPartIdToUiPartId = new HashMap<String, String>();
    m_activateViewLock = new OptimisticLock();
  }

  protected void attachUiListeners() {
    if (m_perspectiveListener == null) {
      m_perspectiveListener = new P_PerspectiveListener();
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(m_perspectiveListener);
    }
  }

  protected void detachUiListeners() {
    if (m_perspectiveListener != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().removePerspectiveListener(m_perspectiveListener);
      m_perspectiveListener = null;
    }
  }

  @Override
  public boolean acquireActivateViewLock() {
    return m_activateViewLock.acquire();
  }

  public boolean isActivateViewLockAcquired() {
    return m_activateViewLock.isAcquired();
  }

  @Override
  public void releaseActivateViewLock() {
    m_activateViewLock.release();
  }

  @Override
  protected synchronized void init(Runnable additionalInitCallback) throws CoreException {
    if (getSubject() == null) {
      Subject subject = Subject.getSubject(AccessController.getContext());
      if (subject == null) {
        throw new SecurityException("/rap request is not authenticated with a Subject");
      }
      setSubject(subject);
    }
    // workbench must exist
    if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
      throw new IllegalStateException("workbench must be active");
    }
    // close views that were opened due to workbench caching the latest layout
    // of views
    for (IWorkbenchWindow workbenchWindow : PlatformUI.getWorkbench().getWorkbenchWindows()) {
      for (IWorkbenchPage workbenchPage : workbenchWindow.getPages()) {
        for (IViewReference viewReference : workbenchPage.getViewReferences()) {
          if (m_scoutPartIdToUiPartId.containsValue(viewReference.getId())) {
            if (workbenchPage.isPartVisible(viewReference.getPart(false))) {
              workbenchPage.hideView(viewReference);
            }
          }
        }
      }
    }
    super.init(additionalInitCallback);
    attachUiListeners();
  }

  @Override
  protected void dispose() {
    super.dispose();
    detachUiListeners();
  }

  @Override
  public void showFormPart(IForm form) {
    if (form == null) {
      return;
    }
    if (form.getDisplayHint() != IForm.DISPLAY_HINT_VIEW) {
      super.showFormPart(form);
      return;
    }

    String scoutViewId = form.getDisplayViewId();
    String uiViewId = getUiPartIdForScoutPartId(scoutViewId);
    if (uiViewId == null) {
      LOG.warn("no view defined for scoutViewId: " + form.getDisplayViewId());
      return;
    }
    IViewPart existingView = findViewPart(uiViewId);

    String formPerspectiveId = form.getPerspectiveId();
    if (formPerspectiveId == null) {
      formPerspectiveId = "";
    }
    IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    //open form if formPerspectiveId is empty
    //       OR if currentPerspectiveId equals perspecitveId set on form
    if (StringUtility.hasText(formPerspectiveId)
        && existingView == null
        && activePage != null
        && CompareUtility.notEquals(activePage.getPerspective().getId(), formPerspectiveId)) {

      synchronized (m_openLaterMap) {
        if (!m_openLaterMap.containsKey(formPerspectiveId) || !m_openLaterMap.get(formPerspectiveId).contains(form)) {
          if (m_openLaterMap.get(formPerspectiveId) == null) {
            m_openLaterMap.put(formPerspectiveId, new ArrayList<IForm>());
          }
          m_openLaterMap.get(formPerspectiveId).add(form);
        }
      }
      return;
    }
    if (IForm.EDITOR_ID.equals(form.getDisplayViewId()) || IWizard.EDITOR_ID.equals(form.getDisplayViewId())) {
      if (activePage != null) {
        ScoutFormEditorInput editorInput = new ScoutFormEditorInput(form, this);
        AbstractScoutEditorPart editor = getEditorPart(editorInput, uiViewId);
        putPart(form, editor);
      }
    }
    else {
      AbstractScoutView view = getViewPart(uiViewId);
      view.showForm(form);
      putPart(form, view);
    }

  }

  @Override
  public Display getDisplay() {
    if (m_display == null) {
      m_display = Display.getDefault();
      if (m_display == null) {
        m_display = new Display();
      }
    }
    return m_display;
  }

  /**
   * @param scoutPartLocation
   *          the location id defined in {@link IForm} or additional.
   * @param uiPartId
   *          the id of the {@link IViewPart} registered in the plugin.xml as a
   *          view extension.
   */
  public void registerPart(String scoutPartLocation, String uiPartId) {
    m_scoutPartIdToUiPartId.put(scoutPartLocation, uiPartId);
  }

  public void unregisterPart(String scoutPartLocation) {
    m_scoutPartIdToUiPartId.remove(scoutPartLocation);
  }

  public final String[] getAllPartIds() {
    HashSet<String> partIds = new HashSet<String>(m_scoutPartIdToUiPartId.values());
    return partIds.toArray(new String[partIds.size()]);
  }

  public final String getUiPartIdForScoutPartId(String scoutPartLocation) {
    return m_scoutPartIdToUiPartId.get(scoutPartLocation);
  }

  public final String getScoutPartIdForUiPartId(String partId) {
    if (partId == null) {
      return "";
    }
    for (Entry<String, String> entry : m_scoutPartIdToUiPartId.entrySet()) {
      if (entry.getValue().equals(partId)) {
        return entry.getKey();
      }
    }
    return "";
  }

  public IViewPart findViewPart(String viewId) {
    if (viewId != null) {
      IViewDescriptor viewRef = PlatformUI.getWorkbench().getViewRegistry().find(viewId);
      if (viewRef != null && !viewRef.getAllowMultiple()) {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewId);
      }
    }
    return null;
  }

  public AbstractScoutEditorPart getEditorPart(IEditorInput editorInput, String editorId) {
    if (editorInput != null && editorId != null) {
      try {
        IEditorPart editor = null;
        editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findEditor(editorInput);

        if (editor == null) {
          editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, editorId);
        }
        if (!(editor instanceof AbstractScoutEditorPart)) {
          LOG.warn("editors used in scout's enviromnent must be extensions of AbstractScoutEditorPart");
        }
        else {
          return (AbstractScoutEditorPart) editor;
        }
      }
      catch (PartInitException e) {
        LOG.error("could not inizialize editor", e);
      }
    }
    return null;
  }

  public AbstractScoutView getViewPart(String viewId) {
    if (viewId != null) {
      String secondaryId = null;
      IViewDescriptor viewRef = PlatformUI.getWorkbench().getViewRegistry().find(viewId);
      if (viewRef.getAllowMultiple()) {
        secondaryId = "" + System.currentTimeMillis();
      }
      try {
        IViewPart view = null;
        if (secondaryId == null) {
          view = findViewPart(viewId);

          if (view == null) {
            view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId);
          }
        }
        else {
          view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
        }
        if (!(view instanceof AbstractScoutView)) {
          LOG.warn("views used in scout's enviromnent must be extensions of AbstractScoutView");
        }
        else {
          return (AbstractScoutView) view;
        }
      }
      catch (PartInitException e) {
        LOG.error("could not inizialize view", e);
      }
    }
    return null;
  }

  @Override
  protected void fireGuiDetachedFromUIInternal() {
    super.fireGuiDetachedFromUIInternal();
    if (getDisplay() != null && !getDisplay().isDisposed()) {
      getDisplay().asyncExec(new P_HideScoutViews());
    }
  }

  protected void fireDesktopActivatedFromUIInternal() {
    if (getScoutDesktop() != null) {
      getScoutDesktop().ensureViewStackVisible();
    }
  }

  @Override
  public Shell getParentShellIgnoringPopups(int modalities) {
    Shell shell = Display.getCurrent().getActiveShell();
    if (shell == null) {
      if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
        shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
      }
    }
    if (shell != null) {
      while (RwtUtility.isPopupShell(shell) && shell.getParent() instanceof Shell) {
        shell = (Shell) shell.getParent();
      }
    }
    // traverse complete tree
    if (shell == null) {
      TreeMap<CompositeLong, Shell> map = new TreeMap<CompositeLong, Shell>();
      for (IWorkbenchWindow w : PlatformUI.getWorkbench().getWorkbenchWindows()) {
        RwtUtility.visitShellTreeRec(w.getShell(), modalities, 0, map);
      }
      if (map.size() > 0) {
        shell = map.get(map.firstKey());
      }
    }

    if (shell != null && shell.getData() instanceof ProgressMonitorDialog) {
      // do also ignore the ProgressMonitorDialog, otherwise there will be some strange behaviors
      // when displaying a shell on top of the ProgressMonitorDialog-shell (f.e. when the
      // ProgressMonitorDialog-shell disappears)
      shell = (Shell) shell.getParent();
    }
    return shell;
  }

  private class P_HideScoutViews implements Runnable {
    @Override
    public void run() {
      IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      if (activePage != null) {
        for (IViewReference viewReference : activePage.getViewReferences()) {
          IViewPart view = viewReference.getView(false);
          if (view != null && view instanceof AbstractScoutView) {
            if (((AbstractScoutView) view).getScoutObject() == null) {
              activePage.hideView(viewReference);
            }
          }
          else if (m_scoutPartIdToUiPartId.containsValue(viewReference.getId())) {
            activePage.hideView(viewReference);
          }
        }
      }
    }
  }

  private class P_PerspectiveListener extends PerspectiveAdapter {
    @Override
    public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      String perspectiveId = perspective.getId();
      if (handlePerspectiveOpened(perspectiveId)) {
        handlePerspectiveActivated(perspectiveId);
      }
    }

    @Override
    public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      // global keystrokes are bound to a perspective so it is necessary to disable the global keystrokes
      if (m_perspectiveId.equals(perspective.getId())) {
        getKeyStrokeManager().setGlobalKeyStrokesActivated(false);
      }
    }

    @Override
    public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      handlePerspectiveClosed(perspective.getId());
    }

    @Override
    public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
      String perspectiveId = perspective.getId();
      //If perspective is resetted make sure that scout views are open
      if (IWorkbenchPage.CHANGE_RESET.equals(changeId)) {
        handlePerspectiveClosed(perspectiveId);
      }
      else if (IWorkbenchPage.CHANGE_RESET_COMPLETE.equals(changeId)) {
        if (handlePerspectiveOpened(perspectiveId)) {
          handlePerspectiveActivated(perspectiveId);
        }
      }
    }
  }

  private synchronized boolean handlePerspectiveOpened(String perspectiveId) {
    if (m_perspectiveId.equals(perspectiveId)) {
      //make sure that the desktop is only started once
      if (!isStartDesktopCalled()) {
        final P_PerspecitveOpenedJob j = new P_PerspecitveOpenedJob(getDesktopOpenedTaskText(), getClientSession());
        j.schedule();
        setStartDesktopCalled(true);
      }
      getKeyStrokeManager().setGlobalKeyStrokesActivated(true);
      return isStartDesktopCalled();
    }
    return false;
  }

  private synchronized boolean handlePerspectiveActivated(String perspectiveId) {
    if (m_openLaterMap.containsKey(perspectiveId)) {
      List<IForm> list;
      synchronized (m_openLaterMap) {
        list = m_openLaterMap.remove(perspectiveId);
      }
      for (IForm form : list) {
        showFormPart(form);
      }
      setActivateDesktopCalled(CompareUtility.notEquals(m_perspectiveId, perspectiveId));
    }

    if (m_perspectiveId.equals(perspectiveId) && isStartDesktopCalled()) {
      //make sure that the desktop is only started once
      if (!isActivateDesktopCalled()) {
        final P_PerspectiveActivatedJob j = new P_PerspectiveActivatedJob(getDesktopOpenedTaskText(), getClientSession());
        j.schedule();
        setActivateDesktopCalled(true);
      }
      getKeyStrokeManager().setGlobalKeyStrokesActivated(true);
      return isActivateDesktopCalled();
    }
    return false;
  }

  private synchronized boolean handlePerspectiveClosed(String perspectiveId) {
    boolean called = false;

    // make sure that the desktop is only started once
    if (m_perspectiveId.equals(perspectiveId)) {
      final P_PerspectiveClosedJob j = new P_PerspectiveClosedJob(getDesktopClosedTaskText(), getClientSession());
      j.schedule();
      called = true;
      setStartDesktopCalled(false);
      setActivateDesktopCalled(false);

      //global keystrokes are bound to a perspective so it is necessary to disable the global keystrokes
      getKeyStrokeManager().setGlobalKeyStrokesActivated(false);
    }
    return called;
  }

  @Override
  protected void handleScoutPrintInRwt(DesktopEvent e) {
    WidgetPrinter wp = new WidgetPrinter(getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS));
    try {
      wp.print(e.getPrintDevice(), e.getPrintParameters());
    }
    catch (Throwable ex) {
      LOG.error(null, ex);
    }
  }

  private final class P_PerspecitveOpenedJob extends ClientAsyncJob {
    public P_PerspecitveOpenedJob(String name, IClientSession session) {
      super(name, session);
    }

    @Override
    protected void runVoid(IProgressMonitor monitor) throws Throwable {
      getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          applyScoutState();
        }
      });
      ClientSyncJob clienSyncJob = new ClientSyncJob(getDesktopOpenedTaskText(), getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor syncMonitor) throws Throwable {
          fireGuiAttachedFromUIInternal();
        }
      };
      clienSyncJob.schedule();
    }
  }

  private final class P_PerspectiveActivatedJob extends ClientAsyncJob {
    public P_PerspectiveActivatedJob(String name, IClientSession session) {
      super(name, session);
    }

    @Override
    protected void runVoid(IProgressMonitor monitor) throws Throwable {
      ClientSyncJob clienSyncJob = new ClientSyncJob(getDesktopOpenedTaskText(), getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor syncMonitor) throws Throwable {
          fireDesktopActivatedFromUIInternal();
        }
      };
      clienSyncJob.schedule();
    }
  }

  private final class P_PerspectiveClosedJob extends ClientAsyncJob {
    public P_PerspectiveClosedJob(String name, IClientSession session) {
      super(name, session);
    }

    @Override
    protected void runVoid(IProgressMonitor monitor) throws Throwable {
      ClientSyncJob clienSyncJob = new ClientSyncJob(getDesktopOpenedTaskText(), getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor syncMonitor) throws Throwable {
          fireGuiDetachedFromUIInternal();
        }
      };
      clienSyncJob.schedule();
    }
  }

}
