/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.BrowserHistoryEntry;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IOpenUriAction;
import org.eclipse.scout.rt.client.ui.desktop.OpenUriAction;
import org.eclipse.scout.rt.client.ui.desktop.bench.layout.BenchLayoutData;
import org.eclipse.scout.rt.client.ui.desktop.notification.IDesktopNotification;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.cache.DownloadHttpResponseInterceptor;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.eclipse.scout.rt.ui.html.json.desktop.DownloadHandlerStorage.BinaryResourceHolderWithAction;
import org.eclipse.scout.rt.ui.html.json.desktop.bench.layout.JsonBenchLayoutData;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonDesktop<DESKTOP extends IDesktop> extends AbstractJsonWidget<DESKTOP> implements IBinaryResourceProvider {

  private static final Logger LOG = LoggerFactory.getLogger(JsonDesktop.class);

  private static final String EVENT_OUTLINE_CHANGED = "outlineChanged";
  private static final String EVENT_OUTLINE_CONTENT_ACTIVATE = "outlineContentActivate";
  private static final String EVENT_HISTORY_ENTRY_ACTIVATE = "historyEntryActivate";
  private static final String EVENT_ADD_NOTIFICATION = "addNotification";
  private static final String EVENT_REMOVE_NOTIFICATION = "removeNotification";
  private static final String EVENT_OPEN_URI = "openUri";
  private static final String EVENT_FORM_ACTIVATE = "formActivate";
  private static final String EVENT_DESKTOP_READY = "desktopReady";
  private static final String EVENT_LOGO_ACTION = "logoAction";
  private static final String EVENT_CANCEL_FORMS = "cancelForms";

  public static final String PROP_OUTLINE = "outline";
  public static final String PROP_DISPLAY_PARENT = "displayParent";
  public static final String PROP_POSITION = "position";
  public static final String PROP_FORM = "form";
  public static final String PROP_MESSAGE_BOX = "messageBox";
  public static final String PROP_NOTIFICATION = "notification";
  public static final String PROP_FILE_CHOOSER = "fileChooser";
  public static final String PROP_ACTIVE_FORM = "activeForm";
  public static final String PROP_BENCH_LAYOUT_DATA = IDesktop.PROP_BENCH_LAYOUT_DATA;

  private static final AtomicLong RESOURCE_COUNTER = new AtomicLong();

  private final DownloadHandlerStorage m_downloads;
  private DesktopListener m_desktopListener;
  private final DesktopEventFilter m_desktopEventFilter;
  private final BooleanPropertyChangeFilter m_browserHistoryFilter = new BooleanPropertyChangeFilter(IDesktop.PROP_BROWSER_HISTORY_ENTRY, false);

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
    attachGlobalAdapters(getModel().getSelectedViews(getModel()));
    attachGlobalAdapters(getModel().getDialogs(getModel(), false));
    attachGlobalAdapters(getModel().getMessageBoxes(getModel()));
    attachGlobalAdapters(getModel().getFileChoosers(getModel()));
    attachAdapters(getModel().getNotifications());
    attachAdapters(getModel().getMenus(), new DisplayableActionFilter<>());
    attachAdapters(getModel().getAddOns());
    attachAdapters(getModel().getKeyStrokes(), new DisplayableActionFilter<>());
    attachAdapters(getModel().getViewButtons(), new DisplayableActionFilter<>());
    attachGlobalAdapter(getModel().getOutline(), new DisplayableOutlineFilter<>());
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_FORM_ACTIVATE.equals(event.getType())) {
      handleUiFormActivate(event);
    }
    else if (EVENT_HISTORY_ENTRY_ACTIVATE.equals(event.getType())) {
      handleUiHistoryEntryActivate(event);
    }
    else if (EVENT_LOGO_ACTION.equals(event.getType())) {
      handleUiLogoAction(event);
    }
    else if (EVENT_CANCEL_FORMS.equals(event.getType())) {
      handleCancelAllForms(event);
    }
    else if (EVENT_DESKTOP_READY.equals(event.getType())) {
      handleUiDesktopReady(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiDesktopReady(JsonEvent event) {
    getModel().getUIFacade().readyFromUI();
  }

  protected void handleUiHistoryEntryActivate(JsonEvent event) {
    addPropertyEventFilterCondition(m_browserHistoryFilter);
    String deepLinkPath = event.getData().optString("deepLinkPath", null);
    getModel().getUIFacade().historyEntryActivateFromUI(deepLinkPath);
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IDesktop.PROP_NAVIGATION_VISIBLE.equals(propertyName)) {
      boolean visible = data.getBoolean(propertyName);
      addPropertyEventFilterCondition(propertyName, visible);
      getModel().getUIFacade().setNavigationVisibleFromUI(visible);
    }
    if (IDesktop.PROP_NAVIGATION_HANDLE_VISIBLE.equals(propertyName)) {
      boolean visible = data.getBoolean(propertyName);
      addPropertyEventFilterCondition(propertyName, visible);
      getModel().getUIFacade().setNavigationHandleVisibleFromUI(visible);
    }
    else if (IDesktop.PROP_BENCH_VISIBLE.equals(propertyName)) {
      boolean visible = data.getBoolean(propertyName);
      addPropertyEventFilterCondition(propertyName, visible);
      getModel().getUIFacade().setBenchVisibleFromUI(visible);
    }
    else if (IDesktop.PROP_HEADER_VISIBLE.equals(propertyName)) {
      boolean visible = data.getBoolean(propertyName);
      addPropertyEventFilterCondition(propertyName, visible);
      getModel().getUIFacade().setHeaderVisibleFromUI(visible);
    }
    else if (IDesktop.PROP_GEOLOCATION_SERVICE_AVAILABLE.equals(propertyName)) {
      boolean available = data.getBoolean(propertyName);
      addPropertyEventFilterCondition(propertyName, available);
      getModel().getUIFacade().setGeoLocationServiceAvailableFromUI(available);
    }
    else if (IDesktop.PROP_IN_BACKGROUND.equals(propertyName)) {
      boolean inBackground = data.getBoolean(propertyName);
      addPropertyEventFilterCondition(propertyName, inBackground);
      getModel().getUIFacade().setInBackgroundFromUI(inBackground);
    }
    else if (IDesktop.PROP_FOCUSED_ELEMENT.equals(propertyName)) {
      String id = data.optString(propertyName, null);
      IWidget focusedElement = getWidgetById(id);
      addPropertyEventFilterCondition(propertyName, focusedElement);
      getModel().getUIFacade().setFocusedElementFromUI(focusedElement);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }

  protected IWidget getWidgetById(String id) {
    if (id == null) {
      return null;
    }
    IJsonAdapter<?> jsonAdapter = getUiSession().getJsonAdapter(id);
    if (jsonAdapter == null) {
      return null;
    }
    return (IWidget) jsonAdapter.getModel();
  }

  protected void handleCancelAllForms(JsonEvent event) {
    JSONArray formIds = event.getData().optJSONArray("formIds");
    HashSet<IForm> formSet = new HashSet<>();
    if (formIds != null) {
      for (int i = 0; i < formIds.length(); i++) {
        String formId = formIds.optString(i, null);
        IJsonAdapter<?> jsonAdapter = getUiSession().getJsonAdapter(formId);
        if (jsonAdapter == null) {
          continue;
        }
        formSet.add((IForm) jsonAdapter.getModel());
      }
    }
    getModel().cancelForms(formSet);
  }

  protected void handleUiLogoAction(JsonEvent event) {
    getModel().getUIFacade().fireLogoAction();
  }

  protected void handleUiFormActivate(JsonEvent event) {
    String formId = event.getData().optString("formId", null);
    if (formId == null) {
      getModel().getUIFacade().activateForm(null);
      return;
    }
    IJsonAdapter<?> jsonAdapter = getUiSession().getJsonAdapter(formId);
    if (jsonAdapter == null) {
      //should not occur, but if it occurs it's not fatal because on next dialog/view/outline opening this is repaired
      LOG.info("handleUIFormActivated is looking for form which exists no more. ID: {}", formId);
      return;
    }
    IForm form = (IForm) jsonAdapter.getModel();
    addDesktopEventFilterCondition(DesktopEvent.TYPE_FORM_ACTIVATE).setForm(form);
    getModel().getUIFacade().activateForm(form);
  }

  protected DesktopEventFilterCondition addDesktopEventFilterCondition(int tableEventType) {
    DesktopEventFilterCondition condition = new DesktopEventFilterCondition(tableEventType);
    condition.setCheckDisplayParents(true);
    m_desktopEventFilter.addCondition(condition);
    return condition;
  }

  @Override
  public void cleanUpEventFilters() {
    super.cleanUpEventFilters();
    m_desktopEventFilter.removeAllConditions();
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_desktopListener != null) {
      throw new IllegalStateException();
    }
    m_desktopListener = new P_DesktopListener();
    getModel().addUIDesktopListener(m_desktopListener);
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
  public void init() {
    super.init();

    // Replay missed events
    IEventHistory<DesktopEvent> eventHistory = getModel().getEventHistory();
    if (eventHistory != null) {
      for (DesktopEvent event : eventHistory.getRecentEvents()) {
        handleModelDesktopEvent(event);
      }
    }
  }

  @Override
  protected void initJsonProperties(DESKTOP model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_TITLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getTitle();
      }
    });
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_SELECT_VIEW_TABS_KEY_STROKES_ENABLED, model) {
      @Override
      protected Object modelValue() {
        return getModel().isSelectViewTabsKeyStrokesEnabled();
      }
    });
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_SELECT_VIEW_TABS_KEY_STROKE_MODIFIER, model) {
      @Override
      protected Object modelValue() {
        return getModel().getSelectViewTabsKeyStrokeModifier();
      }
    });
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_CACHE_SPLITTER_POSITION, model) {
      @Override
      protected Object modelValue() {
        return getModel().isCacheSplitterPosition();
      }
    });
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_BROWSER_HISTORY_ENTRY, model) {
      @Override
      protected BrowserHistoryEntry modelValue() {
        return getModel().getBrowserHistoryEntry();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonBrowserHistoryEntry.toJson((BrowserHistoryEntry) value);
      }
    });
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_LOGO_ID, model) {
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
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_NAVIGATION_VISIBLE, model) {
      @Override
      protected Object modelValue() {
        return getModel().isNavigationVisible();
      }
    });
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_NAVIGATION_HANDLE_VISIBLE, model) {
      @Override
      protected Object modelValue() {
        return getModel().isNavigationHandleVisible();
      }
    });
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_BENCH_VISIBLE, model) {
      @Override
      protected Object modelValue() {
        return getModel().isBenchVisible();
      }
    });
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_HEADER_VISIBLE, model) {
      @Override
      protected Object modelValue() {
        return getModel().isHeaderVisible();
      }
    });
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_GEOLOCATION_SERVICE_AVAILABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isGeolocationServiceAvailable();
      }
    });
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_LOGO_ACTION_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLogoActionEnabled();
      }
    });
    putJsonProperty(new JsonProperty<>(PROP_BENCH_LAYOUT_DATA, model) {
      @Override
      protected BenchLayoutData modelValue() {
        return getModel().getBenchLayoutData();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonBenchLayoutData.toJson((BenchLayoutData) value);
      }

    });
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_DISPLAY_STYLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getDisplayStyle();
      }
    });
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_DENSE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isDense();
      }
    });
    putJsonProperty(new JsonProperty<>(IDesktop.PROP_TRACK_FOCUS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTrackFocus();
      }
    });
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put(IDesktop.PROP_DISPLAY_STYLE, getModel().getDisplayStyle());
    putAdapterIdProperty(json, IDesktop.PROP_ACTIVE_FORM, getModel().getActiveForm());
    putAdapterIdsProperty(json, "views", getModel().getViews(getModel()));
    putAdapterIdsProperty(json, "dialogs", getModel().getDialogs(getModel(), false));
    putAdapterIdsProperty(json, "messageBoxes", getModel().getMessageBoxes(getModel()));
    putAdapterIdsProperty(json, "notifications", getModel().getNotifications());
    putAdapterIdsProperty(json, "fileChoosers", getModel().getFileChoosers(getModel()));
    putAdapterIdsProperty(json, "menus", getModel().getMenus(), new DisplayableActionFilter<>());
    putAdapterIdsProperty(json, "addOns", getModel().getAddOns());
    putAdapterIdsProperty(json, "keyStrokes", getModel().getKeyStrokes(), new DisplayableActionFilter<>());
    putAdapterIdsProperty(json, "viewButtons", getModel().getViewButtons(), new DisplayableActionFilter<>());
    putAdapterIdProperty(json, "outline", getModel().getOutline(), new DisplayableOutlineFilter<>());
    if (!getModel().getSelectedViews(getModel()).isEmpty()) {
      putAdapterIdsProperty(json, "selectedViewTabs", getModel().getSelectedViews(getModel()));
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
      case DesktopEvent.TYPE_RELOAD_GUI:
        handleModelReloadGui();
        break;
      default:
        // NOP
    }
  }

  protected void handleModelNotificationAdded(DesktopEvent event) {
    IDesktopNotification notification = event.getNotification();
    IJsonAdapter<?> jsonAdapter = attachAdapter(notification);
    JSONObject jsonEvent = new JSONObject();
    jsonEvent.put(PROP_NOTIFICATION, jsonAdapter.getId());
    addActionEvent(EVENT_ADD_NOTIFICATION, jsonAdapter, jsonEvent);
  }

  protected void handleModelNotificationRemoved(DesktopEvent event) {
    IDesktopNotification notification = event.getNotification();
    IJsonAdapter<?> jsonAdapter = getAdapter(notification);
    if (jsonAdapter != null) {
      JSONObject jsonEvent = new JSONObject();
      jsonEvent.put(PROP_NOTIFICATION, jsonAdapter.getId());
      addActionEvent(EVENT_REMOVE_NOTIFICATION, jsonAdapter, jsonEvent);
    }
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
    addActionEvent(EVENT_OPEN_URI, json).protect();
  }

  protected void handleModelOpenUri(BinaryResource res, IOpenUriAction openUriAction) {
    // add another path segment to filename to distinguish between different resources
    // with the same filename (also makes hash collisions irrelevant).
    long counter = RESOURCE_COUNTER.getAndIncrement();
    String filenameWithCounter = counter + "/" + ObjectUtility.nvl(cleanFilename(res.getFilename()), "binaryData");
    BinaryResourceHolder holder = new BinaryResourceHolder(res);
    if (openUriAction == OpenUriAction.DOWNLOAD) {
      holder.addHttpResponseInterceptor(new DownloadHttpResponseInterceptor(res.getFilename()));
    }
    m_downloads.put(filenameWithCounter, holder, openUriAction);
    String downloadUrl = BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(this, filenameWithCounter);
    handleModelOpenUri(downloadUrl, openUriAction);
  }

  /**
   * <pre>
   * Filenames should not include certain protected characters, not even encoded. Using protected characters like
   * semicolon (even when encoded), can lead to trouble with reverse-proxies that may decode the URL and then interpret
   * semicolons as path parameters.
   *
   * For further information see:
   * - <a href="https://serverfault.com/questions/874726/apache-decoding-semicolon-mod-proxy">ServerFault</a>
   * - <a href=
   "https://security.stackexchange.com/questions/251723/semicolons-relation-with-reverse-proxy">StackExchange</a>
   * </pre>
   */
  protected String cleanFilename(String fileName) {
    return StringUtility.replace(fileName, ";", "");
  }

  @Override
  public BinaryResourceHolder provideBinaryResource(String filename) {
    BinaryResourceHolderWithAction holderWithAction = m_downloads.get(filename);
    if (holderWithAction != null) {
      return holderWithAction.getHolder();
    }
    return null;
  }

  protected void handleModelOutlineChanged(IOutline outline) {
    String jsonOutlineId = null;
    if (outline != null) {
      IJsonAdapter<?> jsonOutline = attachGlobalAdapter(outline);
      jsonOutlineId = jsonOutline.getId();
    }
    addActionEvent(EVENT_OUTLINE_CHANGED, new JSONObject().put(PROP_OUTLINE, jsonOutlineId));
  }

  protected void handleModelOutlineContentActivate() {
    addActionEvent(EVENT_OUTLINE_CONTENT_ACTIVATE);
  }

  protected void handleModelFormShow(IForm form) {
    if (!form.isShowing()) {
      // If a form has already been closed again (e.g. by another desktop listener), make sure it won't be displayed
      return;
    }
    IJsonAdapter<?> jsonAdapter = attachGlobalAdapter(form);
    // if form is dialog form.getViews(getDisplayParent()).indexOf(form) returns -1 -> position is not sent to UI
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
      List<JsonEvent> events = addActionEventForEachDisplayParentAdapter(EVENT_FORM_ACTIVATE, PROP_FORM, jsonAdapter, form.getDisplayParent(), -1);
      for (JsonEvent event : events) {
        event.protect();
      }
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

  protected void handleModelReloadGui() {
    getUiSession().sendReloadPageEvent();
  }

  protected List<JsonEvent> addActionEventForEachDisplayParentAdapter(String eventName, String propModelAdapterId, IJsonAdapter<?> modelAdapter, IDisplayParent displayParent, int position) {
    List<JsonEvent> events = new ArrayList<>();
    for (IJsonAdapter<IDisplayParent> displayParentAdapter : getUiSession().getJsonAdapters(displayParent)) {
      JSONObject jsonEvent = new JSONObject();
      jsonEvent.put(propModelAdapterId, modelAdapter.getId());
      jsonEvent.put(PROP_DISPLAY_PARENT, displayParentAdapter.getId());
      if (position >= 0) {
        jsonEvent.put(PROP_POSITION, position);
      }
      // Add modelAdapter as "referenced adapter" to event (to remove event when
      // modelAdapter is disposed but desktop is not)
      events.add(addActionEvent(eventName, modelAdapter, jsonEvent));
    }
    return events;
  }

  protected class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent e) {
      ModelJobs.assertModelThread();
      handleModelDesktopEvent(e);
    }
  }
}
