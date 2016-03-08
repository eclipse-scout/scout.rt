/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.BrowserHistory;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop.DesktopStyle;
import org.eclipse.scout.rt.client.ui.desktop.IOpenUriAction;
import org.eclipse.scout.rt.client.ui.desktop.notification.IDesktopNotification;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonDesktop<DESKTOP extends IDesktop> extends AbstractJsonPropertyObserver<DESKTOP> implements IBinaryResourceProvider {

  private static final Logger LOG = LoggerFactory.getLogger(JsonDesktop.class);

  private static final String EVENT_OUTLINE_CHANGED = "outlineChanged";
  private static final String EVENT_OUTLINE_CONTENT_ACTIVATE = "outlineContentActivate";
  private static final String EVENT_FORM_ACTIVATED = "formActivated";

  public static final String PROP_OUTLINE = "outline";
  public static final String PROP_DISPLAY_PARENT = "displayParent";
  public static final String PROP_POSITION = "position";
  public static final String PROP_FORM = "form";
  public static final String PROP_MESSAGE_BOX = "messageBox";
  public static final String PROP_FILE_CHOOSER = "fileChooser";
  public static final String PROP_ACTIVE_FORM = "activeForm";

  private static final AtomicLong RESOURCE_COUNTER = new AtomicLong();

  private final DownloadHandlerStorage m_downloads;
  private DesktopListener m_desktopListener;
  private DesktopEventFilter m_desktopEventFilter;

  public JsonDesktop(DESKTOP desktop, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(desktop, uiSession, id, parent);
    m_downloads = BEANS.get(DownloadHandlerStorage.class);
    m_desktopEventFilter = BEANS.get(DesktopEventFilter.class);
  }

  @Override
  public String getObjectType() {
    return "Desktop";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachGlobalAdapters(getModel().getViews(getModel()));
    attachGlobalAdapters(getModel().getDialogs(getModel(), false));
    attachGlobalAdapters(getModel().getMessageBoxes(getModel()));
    attachGlobalAdapters(getModel().getFileChoosers(getModel()));
    attachAdapters(filterModelActions(), new DisplayableActionFilter<IAction>());
    attachAdapters(getModel().getAddOns());
    attachAdapters(getModel().getKeyStrokes(), new DisplayableActionFilter<IKeyStroke>());
    if (hasDefaultStyle()) {
      attachAdapters(getModel().getViewButtons(), new DisplayableActionFilter<IViewButton>());
      attachGlobalAdapter(getModel().getOutline(), new DisplayableOutlineFilter<IOutline>());
    }
  }

  protected boolean hasDefaultStyle() {
    return DesktopStyle.DEFAULT == getModel().getDesktopStyle();
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_FORM_ACTIVATED.equals(event.getType())) {
      handleUiFormActivated(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiFormActivated(JsonEvent event) {
    String formId = event.getData().optString("formId", null);
    if (formId == null) {
      getModel().activateForm(null);
      return;
    }
    IJsonAdapter<?> jsonAdapter = getUiSession().getJsonAdapter(formId);
    if (jsonAdapter == null) {
      //should not occur, but if it occurs its not fatal because on next dialog/view/outline opening this is repaired
      LOG.info("handleUIFormActivated is looking for form which exists no more. ID: {}", formId);
      return;
    }
    IForm form = (IForm) jsonAdapter.getModel();
    addDesktopEventFilterCondition(DesktopEvent.TYPE_FORM_ACTIVATE).setForm(form);
    getModel().activateForm(form);
  }

  protected DesktopEventFilterCondition addDesktopEventFilterCondition(int tableEventType) {
    DesktopEventFilterCondition conditon = new DesktopEventFilterCondition(tableEventType);
    conditon.setCheckDisplayParents(true);
    m_desktopEventFilter.addCondition(conditon);
    return conditon;
  }

  @Override
  public void cleanUpEventFilters() {
    super.cleanUpEventFilters();
    m_desktopEventFilter.removeAllConditions();
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
    getModel().addDesktopListenerAtExecutionEnd(m_desktopListener);
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
  protected void initJsonProperties(DESKTOP model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<DESKTOP>(IDesktop.PROP_TITLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getTitle();
      }
    });
    putJsonProperty(new JsonProperty<DESKTOP>(IDesktop.PROP_SELECT_VIEW_TABS_KEY_STROKES_ENABLED, model) {
      @Override
      protected Object modelValue() {
        return getModel().isSelectViewTabsKeyStrokesEnabled();
      }
    });
    putJsonProperty(new JsonProperty<DESKTOP>(IDesktop.PROP_SELECT_VIEW_TABS_KEY_STROKE_MODIFIER, model) {
      @Override
      protected Object modelValue() {
        return getModel().getSelectViewTabsKeyStrokeModifier();
      }
    });
    putJsonProperty(new JsonProperty<DESKTOP>(IDesktop.PROP_CACHE_SPLITTER_POSITION, model) {
      @Override
      protected Object modelValue() {
        return getModel().isCacheSplitterPosition();
      }
    });
    putJsonProperty(new JsonProperty<DESKTOP>(IDesktop.PROP_BROWSER_HISTORY, model) {
      @Override
      protected BrowserHistory modelValue() {
        return getModel().getBrowserHistory();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonBrowserHistory.toJson((BrowserHistory) value);
      }
    });
    putJsonProperty(new JsonProperty<DESKTOP>(IDesktop.PROP_LOGO_ID, model) {
      @Override
      protected Object modelValue() {
        return getModel().getLogoId();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return BinaryResourceUrlUtility.createIconUrl((String) value);
      }

      @Override
      public String jsonPropertyName() {
        return "logoUrl";
      }
    });
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put(IDesktop.PROP_DESKTOP_STYLE, getModel().getDesktopStyle());
    putAdapterIdProperty(json, IDesktop.PROP_ACTIVE_FORM, getModel().getActiveForm());
    putAdapterIdsProperty(json, "views", getModel().getViews(getModel()));
    putAdapterIdsProperty(json, "dialogs", getModel().getDialogs(getModel(), false));
    putAdapterIdsProperty(json, "messageBoxes", getModel().getMessageBoxes(getModel()));
    putAdapterIdsProperty(json, "fileChoosers", getModel().getFileChoosers(getModel()));
    putAdapterIdsProperty(json, "actions", filterModelActions(), new DisplayableActionFilter<IAction>());
    putAdapterIdsProperty(json, "addOns", getModel().getAddOns());
    putAdapterIdsProperty(json, "keyStrokes", getModel().getKeyStrokes(), new DisplayableActionFilter<IKeyStroke>());
    if (hasDefaultStyle()) {
      // FIXME cgu: view and tool buttons should be removed from desktop by device transformer
      putAdapterIdsProperty(json, "viewButtons", getModel().getViewButtons(), new DisplayableActionFilter<IViewButton>());
      putAdapterIdProperty(json, "outline", getModel().getOutline(), new DisplayableOutlineFilter<IOutline>());
    }
    return json;
  }

  protected void handleModelDesktopEvent(DesktopEvent event) {
    event = m_desktopEventFilter.filter(event);
    if (event == null) {
      return;
    }
    switch (event.getType()) {
      case DesktopEvent.TYPE_OUTLINE_CHANGED:
        handleModelOutlineChanged(event.getOutline());
        break;
      case DesktopEvent.TYPE_OUTLINE_CONTENT_ACTIVATE:
        handleModelOutlineContentActivate();
        break;
      case DesktopEvent.TYPE_FORM_SHOW:
        handleModelFormShow(event.getForm());
        break;
      case DesktopEvent.TYPE_FORM_HIDE:
        handleModelFormHide(event.getForm());
        break;
      case DesktopEvent.TYPE_FORM_ACTIVATE:
        handleModelFormActivate(event.getForm());
        break;
      case DesktopEvent.TYPE_MESSAGE_BOX_SHOW:
        handleModelMessageBoxShow(event.getMessageBox());
        break;
      case DesktopEvent.TYPE_MESSAGE_BOX_HIDE:
        handleModelMessageBoxHide(event.getMessageBox());
        break;
      case DesktopEvent.TYPE_FILE_CHOOSER_SHOW:
        handleModelFileChooserShow(event.getFileChooser());
        break;
      case DesktopEvent.TYPE_FILE_CHOOSER_HIDE:
        handleModelFileChooserHide(event.getFileChooser());
        break;
      case DesktopEvent.TYPE_OPEN_URI:
        if (event.getUri() != null) {
          handleModelOpenUri(event.getUri(), event.getOpenUriAction());
        }
        else if (event.getBinaryResource() != null) {
          handleModelOpenUri(event.getBinaryResource(), event.getOpenUriAction());
        }
        break;
      case DesktopEvent.TYPE_DESKTOP_CLOSED:
        handleModelDesktopClosed();
        break;
      case DesktopEvent.TYPE_NOTIFICATION_ADDED:
        handleModelNotificationAdded(event);
        break;
      case DesktopEvent.TYPE_NOTIFICATION_REMOVED:
        handleModelNotificationRemoved(event);
        break;
      default:
        // NOP
    }
  }

  protected void handleModelNotificationAdded(DesktopEvent event) {
    IDesktopNotification notification = event.getNotification();
    addActionEvent("addNotification", JsonDesktopNotification.toJson(notification));
  }

  protected void handleModelNotificationRemoved(DesktopEvent event) {
    IDesktopNotification notification = event.getNotification();
    addActionEvent("removeNotification", JsonDesktopNotification.toNotificationIdJson(notification));
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (IDesktop.PROP_THEME.equals(propertyName)) {
      getUiSession().updateTheme((String) newValue);
    }
  }

  protected void handleModelOpenUri(String uri, IOpenUriAction openUriAction) {
    JSONObject json = new JSONObject();
    putProperty(json, "uri", uri);
    putProperty(json, "action", openUriAction.getIdentifier());
    addActionEvent("openUri", json);
  }

  protected void handleModelOpenUri(BinaryResource res, IOpenUriAction openUriAction) {
    String filenameHash = BinaryResourceUrlUtility.getFilenameHash(res.getFilename());
    // add another path segment to filename to distinguish between different resources
    // with the same filename (also makes hash collisions irrelevant).
    long counter = RESOURCE_COUNTER.getAndIncrement();
    filenameHash = counter + "/" + filenameHash;

    m_downloads.put(filenameHash, res);
    String downloadUrl = BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(this, filenameHash);
    handleModelOpenUri(downloadUrl, openUriAction);
  }

  @Override
  public BinaryResourceHolder provideBinaryResource(String filename) {
    BinaryResource res = m_downloads.remove(filename);
    if (res != null) {
      return new BinaryResourceHolder(res, true);
    }
    else {
      return null;
    }
  }

  protected void handleModelOutlineChanged(IOutline outline) {
    if (!hasDefaultStyle()) {
      return;
    }
    IJsonAdapter<?> jsonAdapter = attachGlobalAdapter(outline);
    addActionEvent(EVENT_OUTLINE_CHANGED, new JSONObject().put(PROP_OUTLINE, jsonAdapter.getId()));
  }

  protected void handleModelOutlineContentActivate() {
    if (!hasDefaultStyle()) {
      return;
    }
    addActionEvent(EVENT_OUTLINE_CONTENT_ACTIVATE);
  }

  protected void handleModelFormShow(IForm form) {
    IJsonAdapter<?> jsonAdapter = attachGlobalAdapter(form);
    //if form is dialog form.getViews(getDisplayParent()).indexOf(form) retunrs -1 -> position is not sent to UI
    addActionEventForEachDisplayParentAdapter("formShow", PROP_FORM, jsonAdapter, form.getDisplayParent(), getModel().getViews(form.getDisplayParent()).indexOf(form));
  }

  protected void handleModelFormHide(IForm form) {
    IJsonAdapter<?> jsonAdapter = getGlobalAdapter(form);
    if (jsonAdapter != null) {
      addActionEventForEachDisplayParentAdapter("formHide", PROP_FORM, jsonAdapter, form.getDisplayParent(), -1);
    }
  }

  protected void handleModelFormActivate(IForm form) {
    IJsonAdapter<?> jsonAdapter = getGlobalAdapter(form);
    if (jsonAdapter != null) {
      addActionEventForEachDisplayParentAdapter("formActivate", PROP_FORM, jsonAdapter, form.getDisplayParent(), -1);
    }
  }

  protected void handleModelMessageBoxShow(final IMessageBox messageBox) {
    IJsonAdapter<?> jsonAdapter = attachGlobalAdapter(messageBox);
    addActionEventForEachDisplayParentAdapter("messageBoxShow", PROP_MESSAGE_BOX, jsonAdapter, messageBox.getDisplayParent(), -1);
  }

  protected void handleModelMessageBoxHide(final IMessageBox messageBox) {
    IJsonAdapter<?> jsonAdapter = getGlobalAdapter(messageBox);
    if (jsonAdapter != null) {
      addActionEventForEachDisplayParentAdapter("messageBoxHide", PROP_MESSAGE_BOX, jsonAdapter, messageBox.getDisplayParent(), -1);
    }
  }

  protected void handleModelFileChooserShow(final IFileChooser fileChooser) {
    IJsonAdapter<?> jsonAdapter = attachGlobalAdapter(fileChooser);
    addActionEventForEachDisplayParentAdapter("fileChooserShow", PROP_FILE_CHOOSER, jsonAdapter, fileChooser.getDisplayParent(), -1);
  }

  protected void handleModelFileChooserHide(final IFileChooser fileChooser) {
    IJsonAdapter<?> jsonAdapter = getGlobalAdapter(fileChooser);
    if (jsonAdapter != null) {
      addActionEventForEachDisplayParentAdapter("fileChooserHide", PROP_FILE_CHOOSER, jsonAdapter, fileChooser.getDisplayParent(), -1);
    }
  }

  protected void handleModelDesktopClosed() {
    // No need to dispose the JsonDesktop. It is disposed automatically
    // when the JsonClientSession is disposed.
  }

  protected void addActionEventForEachDisplayParentAdapter(String eventName, String propModelAdapterId, IJsonAdapter<?> modelAdapter, IDisplayParent displayParent, int position) {
    for (IJsonAdapter<IDisplayParent> displayParentAdapter : getUiSession().getJsonAdapters(displayParent)) {
      JSONObject jsonEvent = new JSONObject();
      jsonEvent.put(propModelAdapterId, modelAdapter.getId());
      jsonEvent.put(PROP_DISPLAY_PARENT, displayParentAdapter.getId());
      if (position >= 0) {
        jsonEvent.put(PROP_POSITION, position);
      }
      // Add modelAdapter as "referenced adapter" to event (to remove event when
      // modelAdapter is disposed but desktop is not)
      addActionEvent(eventName, modelAdapter, jsonEvent);
    }
  }

  protected class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent e) {
      handleModelDesktopEvent(e);
    }
  }
}
