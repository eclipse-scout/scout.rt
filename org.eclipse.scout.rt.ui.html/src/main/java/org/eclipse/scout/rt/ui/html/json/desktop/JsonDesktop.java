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
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.OrderedCollection;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop.DesktopStyle;
import org.eclipse.scout.rt.client.ui.desktop.IDownloadHandler;
import org.eclipse.scout.rt.client.ui.desktop.ITargetWindow;
import org.eclipse.scout.rt.client.ui.desktop.TargetWindow;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTreeForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;
import org.json.JSONObject;

public class JsonDesktop<T extends IDesktop> extends AbstractJsonPropertyObserver<T> implements IBinaryResourceProvider {

  private static final String EVENT_OUTLINE_CHANGED = "outlineChanged";

  public static final String PROP_OUTLINE = "outline";
  public static final String PROP_FORM = "form";
  public static final String PROP_MESSAGE_BOX = "messageBox";
  public static final String PROP_FILE_CHOOSER = "fileChooser";
  public static final String PROP_ACTIVE_FORM = "activeForm";

  private DownloadHandlerStorage m_downloads;
  private DesktopListener m_desktopListener;

  public JsonDesktop(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
    m_downloads = new DownloadHandlerStorage();
  }

  @Override
  public String getObjectType() {
    return "Desktop";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachGlobalAdapters(getViews());
    attachGlobalAdapters(getDialogs());
    attachGlobalAdapters(getModel().getMessageBoxStack());
    attachGlobalAdapters(getModel().getFileChooserStack());
    attachAdapters(filterModelActions(), new DisplayableActionFilter<IAction>());
    attachAdapters(getModel().getAddOns());
    attachAdapters(getModel().getKeyStrokes(), new DisplayableActionFilter<IKeyStroke>());
    if (hasDefaultStyle()) {
      attachAdapters(getModel().getViewButtons(), new DisplayableActionFilter<IViewButton>());
      attachGlobalAdapter(getModel().getOutline(), new DisplayableOutlineFilter<IOutline>());
    }
  }

  private boolean hasDefaultStyle() {
    return DesktopStyle.DEFAULT == getModel().getDesktopStyle();
  }

  /**
   * Returns all filtered list of all {@link IAction}s provided by the desktop. The list does <b>not</b> include
   * {@link IKeyStroke}s and {@link IViewButton}s, because those action types are handled separately. The returned list
   * is ordered according to the actions {@link Order} annotation.
   */
  protected List<IAction> filterModelActions() {
    OrderedCollection<IAction> result = new OrderedCollection<>();
    for (IAction action : getModel().getActions()) {
      if (action instanceof IKeyStroke || action instanceof IViewButton) {
        continue; // skip
      }
      result.addOrdered(action);
    }
    return result.getOrderedList();
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_desktopListener != null) {
      throw new IllegalStateException();
    }
    m_desktopListener = new P_DesktopListener();
    getModel().addDesktopListener(m_desktopListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_desktopListener == null) {
      throw new IllegalStateException();
    }
    getModel().removeDesktopListener(m_desktopListener);
    m_desktopListener = null;
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<T>(IDesktop.PROP_TITLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getTitle();
      }
    });
    putJsonProperty(new JsonProperty<T>(IDesktop.PROP_AUTO_TAB_KEY_STROKES_ENABLED, model) {
      @Override
      protected Object modelValue() {
        return getModel().isAutoTabKeyStrokesEnabled();
      }
    });
    putJsonProperty(new JsonProperty<T>(IDesktop.PROP_AUTO_TAB_KEY_STROKE_MODIFIER, model) {
      @Override
      protected Object modelValue() {
        return getModel().getAutoTabKeyStrokeModifier();
      }
    });
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put(IDesktop.PROP_DESKTOP_STYLE, getModel().getDesktopStyle());
    putAdapterIdsProperty(json, "views", getViews());
    putAdapterIdsProperty(json, "dialogs", getDialogs());
    putAdapterIdsProperty(json, "messageBoxes", getModel().getMessageBoxStack());
    putAdapterIdsProperty(json, "fileChoosers", getModel().getFileChooserStack());
    putAdapterIdsProperty(json, "actions", filterModelActions(), new DisplayableActionFilter<IAction>());
    putAdapterIdsProperty(json, "addOns", getModel().getAddOns());
    putAdapterIdsProperty(json, "keyStrokes", getModel().getKeyStrokes(), new DisplayableActionFilter<IKeyStroke>());
    if (hasDefaultStyle()) {
      // FIXME CGU: view and tool buttons should be removed from desktop by device transformer
      putAdapterIdsProperty(json, "viewButtons", getModel().getViewButtons(), new DisplayableActionFilter<IViewButton>());
      putAdapterIdProperty(json, "outline", getModel().getOutline(), new DisplayableOutlineFilter<IOutline>());
    }
    return json;
  }

  protected List<IForm> getViews() {
    List<IForm> views = new ArrayList<>();
    for (IForm form : getModel().getViewStack()) {
      if (!isFormBlocked(form)) {
        views.add(form);
      }
    }
    return views;
  }

  protected List<IForm> getDialogs() {
    List<IForm> dialogs = new ArrayList<>();
    for (IForm form : getModel().getDialogStack()) {
      if (!isFormBlocked(form)) {
        dialogs.add(form);
      }
    }
    return dialogs;
  }

  protected boolean isFormBlocked(IForm form) {
    // FIXME CGU: ignore desktop forms for the moment, should not be done here, application should handle it or abstractDesktop
    return (hasDefaultStyle() && (form instanceof IOutlineTableForm || form instanceof IOutlineTreeForm));
  }

  protected void handleModelDesktopEvent(DesktopEvent event) {
    switch (event.getType()) {
      case DesktopEvent.TYPE_OUTLINE_CHANGED:
        handleModelOutlineChanged(event.getOutline());
        break;
      case DesktopEvent.TYPE_FORM_SHOW:
        handleModelFormAdded(event.getForm());
        break;
      case DesktopEvent.TYPE_FORM_HIDE:
        handleModelFormRemoved(event.getForm());
        break;
      case DesktopEvent.TYPE_FORM_ENSURE_VISIBLE:
        handleModelFormEnsureVisible(event.getForm());
        break;
      case DesktopEvent.TYPE_OPEN_URI:
        handleModelOpenUri(event.getUri(), event.getTarget());
        break;
      case DesktopEvent.TYPE_DOWNLOAD_RESOURCE:
        handleModelDownloadResource(event.getDownloadHandler());
        break;
      case DesktopEvent.TYPE_MESSAGE_BOX_ADDED:
        handleModelMessageBoxAdded(event.getMessageBox());
        break;
      case DesktopEvent.TYPE_DESKTOP_CLOSED:
        handleModelDesktopClosed();
        break;
      case DesktopEvent.TYPE_FILE_CHOOSER_ADDED:
        handleModelFileChooserAdded(event.getFileChooser());
        break;
      default:
        // NOP
    }
  }

  protected void handleModelOpenUri(String uri, ITargetWindow target) {
    // Note: property 'target' is reserved by Scouts JSON protocol
    JSONObject json = JsonObjectUtility.newOrderedJSONObject();
    putProperty(json, "uri", uri);
    putProperty(json, "uriTarget", target.getIdentifier());
    addActionEvent("openUri", json);
  }

  /**
   * Target for download URL is AUTO because it depends on the type of browser if the browser opens a new window for the
   * downloaded resource or if the download is processed in the same window as the Scout application.
   */
  protected void handleModelDownloadResource(IDownloadHandler handler) {
    String fileName = handler.getResource().getFilename();
    m_downloads.put(fileName, handler);
    String downloadUrl = BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(this, fileName);
    handleModelOpenUri(downloadUrl, TargetWindow.BLANK); // FIXME AWE: (from imo) AUTO causes in
  }

  @Override
  public BinaryResourceHolder provideBinaryResource(String filename) {
    IDownloadHandler handler = m_downloads.remove(filename);
    if (handler != null) {
      return new BinaryResourceHolder(handler.getResource(), true);
    }
    else {
      return null;
    }
  }

  protected void handleModelOutlineChanged(IOutline outline) {
    if (!hasDefaultStyle()) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    IJsonAdapter<?> jsonAdapter = attachGlobalAdapter(outline);
    putProperty(jsonEvent, PROP_OUTLINE, jsonAdapter.getId());
    addActionEvent(EVENT_OUTLINE_CHANGED, jsonEvent);
  }

  protected void handleModelFormAdded(IForm form) {
    if (isFormBlocked(form)) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    IJsonAdapter<?> jsonAdapter = attachGlobalAdapter(form);
    putProperty(jsonEvent, PROP_FORM, jsonAdapter.getId());
    addActionEvent("formAdded", jsonEvent);
  }

  protected void handleModelFormRemoved(IForm form) {
    IJsonAdapter<?> jsonAdapter = getAdapter(form);
    // When the form is closed, it is disposed automatically. Therefore, we cannot resolve
    // the adapter anymore, hence the null check. (This is no problem, because 'formClosed'
    // event implies 'formRemoved', see Form.js/_onFormClosed.)
    if (jsonAdapter != null) {
      JSONObject jsonEvent = new JSONObject();
      putProperty(jsonEvent, PROP_FORM, jsonAdapter.getId());
      addActionEvent("formRemoved", jsonEvent);
    }
  }

  protected void handleModelFormEnsureVisible(IForm form) {
    JSONObject jsonEvent = new JSONObject();
    IJsonAdapter<?> jsonAdapter = getAdapter(form);
    putProperty(jsonEvent, PROP_FORM, jsonAdapter.getId());
    addActionEvent("formEnsureVisible", jsonEvent);
  }

  protected void handleModelMessageBoxAdded(final IMessageBox messageBox) {
    JSONObject jsonEvent = new JSONObject();
    IJsonAdapter<?> jsonAdapter = attachGlobalAdapter(messageBox);
    putProperty(jsonEvent, PROP_MESSAGE_BOX, jsonAdapter.getId());
    addActionEvent("messageBoxAdded", jsonEvent);
  }

  protected void handleModelDesktopClosed() {
    getUiSession().logout();
  }

  protected void handleModelFileChooserAdded(IFileChooser fileChooser) {
    JSONObject jsonEvent = new JSONObject();
    IJsonAdapter<?> jsonAdapter = attachGlobalAdapter(fileChooser);
    putProperty(jsonEvent, PROP_FILE_CHOOSER, jsonAdapter.getId());
    addActionEvent("fileChooserAdded", jsonEvent);
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_OUTLINE_CHANGED.equals(event.getType())) {
      handleUiOutlineChanged(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiOutlineChanged(JsonEvent event) {
    if (!hasDefaultStyle()) {
      return;
    }
    String outlineId = event.getData().getString("outlineId");
    IJsonAdapter<?> jsonOutline = getUiSession().getJsonAdapter(outlineId);
    getModel().setOutline((IOutline) jsonOutline.getModel());
  }

  protected class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent e) {
      handleModelDesktopEvent(e);
    }
  }
}
