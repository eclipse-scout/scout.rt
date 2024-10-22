/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form;

import java.security.Permission;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.datachange.IDataChangeObserver;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormFieldFilter;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.client.ui.wizard.IWizard;
import org.eclipse.scout.rt.client.ui.wizard.IWizardStep;
import org.eclipse.scout.rt.platform.classid.ITypeWithSettableClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.eclipse.scout.rt.platform.reflect.IPropertyFilter;
import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A form is the model used for anything containing fields: a dialog, a step of a wizard, the details of a page, a
 * search. Each form is represented by a class. A form has fields, buttons, and handlers. These elements are all part of
 * the same class, they are <b>inner classes</b> of the form.
 * <p>
 * A <b>field</b> is where you enter data: a string, a number, a date, a list of values. A <b>button</b> is where you
 * trigger actions: save data, cancel whatever you are doing, go on to the next or return to the previous step. A
 * <b>handler</b> is responsible for loading from data and storing data. This usually involves calling process services
 * on the server. These will in turn contact a persistence layer such as a database.
 */
public interface IForm extends IWidget, ITypeWithSettableClassId, IStyleable, IDisplayParent, IDataChangeObserver {

  /**
   * The {@link IForm} which is currently associated with the current thread.
   */
  ThreadLocal<IForm> CURRENT = new ThreadLocal<>();

  String PROP_TITLE = "title";
  String PROP_SUB_TITLE = "subTitle";
  String PROP_HEADER_VISIBLE = "headerVisible";
  String PROP_MAXIMIZED = "maximized";
  String PROP_EMPTY = "empty";
  String PROP_SAVE_NEEDED = "saveNeeded";
  String PROP_ICON_ID = "iconId";
  String PROP_CLOSABLE = "closable";
  String PROP_SAVE_NEEDED_VISIBLE = "saveNeededVisible";
  String PROP_STATUS = "status";

  /**
   * Hint to render an {@link IForm} as dialog in a separate window. A dialog can be modal or non-modal.
   */
  int DISPLAY_HINT_DIALOG = 0;

  /**
   * Hint to render an {@link IForm} as native (browser-) window, which can be positioned on a second screen. A
   * popup-window can be modal or non-modal.
   */
  int DISPLAY_HINT_POPUP_WINDOW = 10;

  /**
   * Hint to render an {@link IForm} as view. Typically, a view is not modal and has the {@link IDesktop} as its
   * {@link IDisplayParent}.
   */
  int DISPLAY_HINT_VIEW = 20;

  /**
   * Hint to auto-derive the <code>modality-state</code> based on the {@link IForm}'s <code>display-hint</code>, meaning
   * <code>true</code> for dialogs and <code>false</code> for views.
   */
  int MODALITY_HINT_AUTO = 0;

  /**
   * Hint to render a {@link IForm} in modal state.
   */
  int MODALITY_HINT_MODAL = 10;

  /**
   * Hint to render a {@link IForm} in non-modal state.
   */
  int MODALITY_HINT_MODELESS = 20;

  String VIEW_ID_N = "N";
  String VIEW_ID_NE = "NE";
  String VIEW_ID_E = "E";
  String VIEW_ID_SE = "SE";
  String VIEW_ID_S = "S";
  String VIEW_ID_SW = "SW";
  String VIEW_ID_W = "W";
  String VIEW_ID_NW = "NW";
  String VIEW_ID_CENTER = "C";
  String VIEW_ID_OUTLINE = "OUTLINE";
  String VIEW_ID_OUTLINE_SELECTOR = "OUTLINE_SELECTOR";
  String VIEW_ID_PAGE_DETAIL = "PAGE_DETAIL";
  String VIEW_ID_PAGE_SEARCH = "PAGE_SEARCH";
  String VIEW_ID_PAGE_TABLE = "PAGE_TABLE";

  /**
   * Initialize the form and all of its fields. By default, any of the #start* methods of the form call this method
   */
  @Override
  void init();

  /**
   * This method is called to get an exclusive key of the form. The key is used to open the same form with the same
   * handler only once. Obviously this behavior can only be used for view forms.
   *
   * @see AbstractDesktop#getSimilarForms(IForm)
   * @return null for exclusive form behavior an exclusive key to ensure similar handling.
   */
  Object computeExclusiveKey();

  /**
   * Activate the form in terms of UI visibility / focus target<br>
   * This will send a desktop event {@link DesktopEvent#TYPE_FORM_ACTIVATE}
   */
  void activate();

  /**
   * Puts the form to front in terms of UI visibility<br>
   * This will send a desktop event {@link FormEvent#TYPE_TO_FRONT}
   *
   * @since 06.07.2009
   */
  void toFront();

  /**
   * Puts the form to back in terms of UI visibility<br>
   * This will send a desktop event {@link FormEvent#TYPE_TO_BACK}
   */
  void toBack();

  void setVisiblePermission(Permission p);

  boolean isVisibleGranted();

  void setVisibleGranted(boolean b);

  String getFormId();

  IFormHandler getHandler();

  void setHandler(IFormHandler handler);

  String getIconId();

  void setIconId(String s);

  /**
   * A closable form is thought to display a close button either in the header of a dialog or a view.
   *
   * @return whether the form is closable.
   */
  boolean isClosable();

  void setClosable(boolean closable);

  /**
   * Whether a form should display the save needed (dirty) state in the dialog or view header.
   *
   * @return true to display the save needed state, false otherwise.
   */
  boolean isSaveNeededVisible();

  void setSaveNeededVisible(boolean saveNeededVisible);

  IMultiStatus getStatus();

  /**
   * @return true if the status is present recursive in all children.
   */
  boolean hasStatus(IStatus status);

  void setStatus(IMultiStatus status);

  void clearStatus();

  void addStatus(IStatus newStatus);

  void removeStatus(IStatus status);

  /**
   * If set, the text will be rendered as notification badge in the right upper corner of the view.
   *
   * @return the text to be display in the notification badge of the form.
   */
  String getNotificationBadgeText();

  void setNotificationBadgeText(String notificationBadgeText);

  /**
   * @return the {@link IWizard} that contains the step that started this form using startWizardStep
   */
  IWizard getWizard();

  /**
   * @return the {@link IWizardStep} that started this form using startWizardStep
   */
  IWizardStep getWizardStep();

  /**
   * @param wizardStep
   *          the step that starts this form. The form can then access the step with {@link #getWizardStep()}.
   * @param handlerType
   *          the inner handler type used to load / store the form. An instance of this handler type is created
   *          automatically and set to the form using {@link #setHandler(IFormHandler)}. If this parameter is
   *          <code>null</code>, the current handler is used instead.
   */
  void startWizardStep(IWizardStep wizardStep, Class<? extends IFormHandler> handlerType);

  /**
   * Like {@link #startWizardStep(IWizardStep, Class)} but without a custom handler type (uses the currently set
   * handler).
   */
  void startWizardStep(IWizardStep<?> wizardStep);

  /**
   * Starts the form using {@link #getHandler()}.
   *
   * @throws ProcessingException
   *           if an error occurs in the handler.
   */
  void start();

  /**
   * @return <code>true</code> if this {@link IForm} is currently attached to the {@link IDesktop} and displayed.
   *         However, a value of <code>true</code> does not imply that it is the currently active {@link IForm}.
   * @see IDesktop#showForm(IForm)
   */
  boolean isShowing();

  /**
   * @return true if the form is not (yet) started with a form handler and therefore not active
   */
  boolean isFormClosed();

  /**
   * @return <code>true</code> if this {@link IForm} is started with a {@link IFormHandler}. However, it does not imply
   *         that it is attached to the {@link IDesktop} and displayed in the UI.
   */
  boolean isFormStarted();

  /**
   * @return <code>true</code> if it is allowed to call the start() method of this form, false otherwise. This method
   *         checks two things: 1 - is the form started yet? 2 - is the blocking condition of the form blocking? Use
   *         this method when you must call the start method of the form, and you're not completely sure in which state
   *         the form currently is.
   */
  boolean isFormStartable();

  /**
   * true while the {@link AbstractFormHandler#execLoad()} method is running<br>
   * this is often used in {@link AbstractValueField#execChangedValue()}
   */
  boolean isFormLoading();

  /**
   * Creates an empty form data.
   */
  AbstractFormData createFormData();

  /**
   * fill a FormData structure to be sent to the backend<br>
   * the configurator is creating typed subclasses of FormData and FormFieldData
   */
  void exportFormData(AbstractFormData target);

  /**
   * apply FormData to this form
   *
   * @param source
   *          the FormData to import
   */
  void importFormData(AbstractFormData source);

  /**
   * apply FormData to this form
   *
   * @param source
   *          the FormData to import
   * @param valueChangeTriggersEnabled
   *          specifies if the {@link AbstractFormField}.execChangedValue should be called on a field value change
   *          caused by this import.
   */
  void importFormData(AbstractFormData source, boolean valueChangeTriggersEnabled);

  /**
   * apply FormData to this form
   *
   * @param source
   *          the FormData to import
   * @param valueChangeTriggersEnabled
   *          specifies if the {@link AbstractFormField}.execChangedValue should be called on a field value change
   *          caused by this import.
   * @param filter
   *          a filter that can be used to specify which form properties should be imported
   * @see IPropertyFilter
   */
  void importFormData(AbstractFormData source, boolean valueChangeTriggersEnabled, IPropertyFilter filter);

  /**
   * apply FormData to this form
   *
   * @param source
   *          the FormData to import
   * @param valueChangeTriggersEnabled
   *          specifies if the {@link AbstractFormField}.execChangedValue should be called on a field value change
   *          caused by this import.
   * @param filter
   *          a filter that can be used to specify which form properties should be imported
   * @param formFieldFilter
   *          a filter that can be used to specify which form fields should be imported
   * @see IPropertyFilter#accept(FastPropertyDescriptor)
   * @see IFormFieldFilter#accept(IFormField)
   */
  void importFormData(AbstractFormData source, boolean valueChangeTriggersEnabled, IPropertyFilter filter, IFormFieldFilter formFieldFilter);

  /**
   * traverse all fields recursive and return them as a list
   */
  List<IFormField> getAllFields();

  List<? extends IFormField> getInvalidFields();

  void validateForm();

  IGroupBox getRootGroupBox();

  /**
   * @return the outer form if this form is inside a {@link IWrappedFormField}
   */
  IForm getOuterForm();

  /**
   * @return the outer form field if this form is inside a {@link IWrappedFormField}
   */
  IWrappedFormField getOuterFormField();

  /**
   * the field ID is the simple class name of a field without the suffixes "Box", "Field", "Button"
   */
  IFormField getFieldById(final String id);

  /**
   * the field ID is the simple class name of a field without the suffixes "Box", "Field", "Button" The field must be
   * equal or a subtype of type
   */
  <T extends IFormField> T getFieldById(String id, Class<T> type);

  /**
   * @return the field with the exact type c in the subtree
   */
  <T extends IFormField> T getFieldByClass(Class<T> c);

  /**
   * see {@link FormEvent#TYPE_STRUCTURE_CHANGED}
   */
  void structureChanged(IFormField causingField);

  void doClose();

  void doCancel();

  /**
   * Save data and close the form.
   */
  void doOk();

  /**
   * Validate the form, save it, and make all fields as saved. The net result is that calling this method again on the
   * unchanged form will revalidate it, but will no longer save it.
   */
  void doSave();

  /**
   * similar to {@link #doSave()} but do NOT set fields to state {@link #isSaveNeeded()}=false
   */
  void doSaveWithoutMarkerChange();

  void doReset();

  void doFinally();

  void doExportXml(boolean saveAs);

  void doImportXml();

  /**
   * @return <code>true</code> if this {@link IForm} should be displayed once being started.
   */
  boolean isShowOnStart();

  /**
   * Controls whether to show this {@link IForm} once started.
   * <p>
   * If set to <code>true</code> and this {@link IForm} is started, it is added to the {@link IDesktop} in order to be
   * displayed. By default, this property is set to <code>true</code>.
   *
   * @param showOnStart
   *          <code>true</code> to show this {@link IForm} on startup, <code>false</code> otherwise.
   * @see IDesktop#showForm(IForm)
   */
  void setShowOnStart(boolean showOnStart);

  void setCloseTimer(int seconds);

  void removeCloseTimer();

  void setTimer(String timerId, int seconds);

  void removeTimer(String timerId);

  void setAllEnabled(boolean b);

  String getCancelVerificationText();

  void setCancelVerificationText(String text);

  /**
   * Determines whether a save is needed for the form.
   */
  boolean isSaveNeeded();

  /**
   * Calls execCheckSaveNeeded on every field to ensure that {@link #isSaveNeeded()} returns the correct value.
   */
  void checkSaveNeeded();

  /**
   * Marks the form as saved so that a save is no longer needed.
   */
  void markSaved();

  /**
   * Touches the form so that a save is needed.
   */
  void touch();

  boolean isEmpty();

  Boolean isHeaderVisible();

  void setHeaderVisible(Boolean visible);

  String getTitle();

  void setTitle(String title);

  String getSubTitle();

  void setSubTitle(String subTitle);

  /**
   * The system button (type) that triggered the save
   *
   * @see "<code>IButton.SYSTEM_TYPE_*</code> constants"
   */
  int getCloseSystemType();

  void setCloseSystemType(int saveType);

  boolean isMaximized();

  void setMaximized(boolean maximized);

  /**
   * @return <code>true</code> to make this {@link IForm} modal in respect to its {@link IDisplayParent}, or
   *         <code>false</code> otherwise.
   */
  boolean isModal();

  /**
   * Sets the modality hint to make this {@link IForm} modal or modeless in respect to its {@link IDisplayParent}.
   */
  void setModal(boolean modal);

  void setCacheBounds(boolean cacheBounds);

  boolean isCacheBounds();

  /**
   * Computes a key which is used when saving the bounds of the form.
   *
   * @return a key to distinguish the forms. It must not be null.
   */
  String computeCacheBoundsKey();

  /**
   * @return the display hint to control visualization of this {@link IForm}.
   * @see #DISPLAY_HINT_VIEW
   * @see #DISPLAY_HINT_POPUP_WINDOW
   */
  int getDisplayHint();

  /**
   * Set the given hint to control visualization of this {@link IForm}.
   * <ul>
   * <li>{@link #DISPLAY_HINT_VIEW}</li>
   * <li>{@link #DISPLAY_HINT_POPUP_WINDOW}</li>
   * </ul>
   */
  void setDisplayHint(int i);

  /**
   * @return the {@link IDisplayParent} to attach this {@link IForm} to; is never <code>null</code>.
   */
  IDisplayParent getDisplayParent();

  /**
   * Sets the display parent to attach this {@link IForm} to.
   * <p>
   * A display parent is the anchor to attach this {@link IForm} to, and affects its accessibility and modality scope.
   * Possible parents are {@link IDesktop}, {@link IOutline}, or {@link IForm}:
   * <ul>
   * <li>Desktop: Form is always accessible; blocks the entire desktop if modal;</li>
   * <li>Outline: Form is only accessible when the given outline is active; only blocks the outline if modal;</li>
   * <li>Form: Form is only accessible when the given Form is active; only blocks the Form if modal;</li>
   * </ul>
   * This property can be changed even if the {@link IForm} is showing.
   *
   * @param displayParent
   *          like {@link IDesktop}, {@link IOutline}, {@link IForm}, or <code>null</code> to use the
   *          {@link IDisplayParent} resolved from the current calling context during initialization.
   */
  void setDisplayParent(IDisplayParent displayParent);

  /**
   * @return the display hint of this {@link IForm}.
   */
  String getDisplayViewId();

  /**
   * use one ofe the VIEW_ID_ constants or a custom text
   */
  void setDisplayViewId(String viewId);

  boolean isAskIfNeedSave();

  void setAskIfNeedSave(boolean b);

  boolean isButtonsArmed();

  void setButtonsArmed(boolean b);

  boolean isCloseTimerArmed();

  void setCloseTimerArmed(boolean b);

  /**
   * marker property to signal that the form changed some data (using services, backend)<br>
   * property is normally used after the form handler is returned
   */
  boolean isFormStored();

  /**
   * marker property to signal that the form changed some data (using services, backend)<br>
   * this property is automatically set whenever the form has called the handlers execStore (disregarding whether this
   * was successful or not)
   */
  void setFormStored(boolean b);

  /**
   * With this method it's possible to set (custom) properties.
   * <p>
   * <b>Important: </b> Although this method is intended to be used for custom properties, it's actually possible to
   * change main properties as well. Keep in mind that directly changing main properties may result in unexpected
   * behavior, so do it only if you really know what you are doing. Rather use the officially provided api instead. <br>
   * Example for an unexpected behavior: setVisible() does not only set the property PROP_VISIBLE but also executes
   * additional code. This code would NOT be executed by directly setting the property PROP_VISIBLE with setProperty().
   *
   * @return true if property value changed
   */
  @Override
  boolean setProperty(String name, Object value);

  /**
   * See {@link #loadFromXml(Element)}
   */
  boolean loadFromXmlString(String xml);

  String storeToXmlString();

  Document storeToXml();

  void storeToXml(Element root);

  /**
   * Imports the form state from an XML document.
   *
   * @param root
   *          The XML document that contains the form state.
   * @return True, if the XML document was loaded successfully. All fields and properties in the XML document which
   *         contain values were found on the form and loaded successfully.
   */
  boolean loadFromXml(Element root);

  /**
   * Wait until form is closed<br>
   * If the form is modal this method returns just after the modal handler has terminated<br>
   * If the form is non-modal this starts a sub event dispatcher that loops (and blocks) until form handling is false
   * (i.e. form has been closed)
   */
  void waitFor();

  int WAIT_FOR_ERROR_CODE = 69218;

  /**
   * reset, create, validate the search model that contains the form data, verbose search texts and a valid status see
   * {@link SearchFilter#clear()} see {@link #doSaveWithoutMarkerChange()} is also resetting the search model
   */
  void resetSearchFilter();

  /**
   * @return life reference to the filter (never null)<br>
   *         For search filter validity check {@link SearchFilter#isCompleted()} in combination with
   *         {@link org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable#isSearchRequired()}
   */
  SearchFilter getSearchFilter();

  void setSearchFilter(SearchFilter filter);

  /**
   * Request focus for the field by sending a {@link FormEvent#TYPE_REQUEST_FOCUS} event.
   */
  void requestFocus(IFormField field);

  /**
   * Request input mode for the field by sending a {@link FormEvent#TYPE_REQUEST_INPUT} event. For most fields request
   * input does the same thing as request focus. But some fields, e.g. the SmartField, may decide to open a popup in
   * order to get input for the field.
   */
  void requestInput(IFormField field);

  FormListeners formListeners();

  /**
   * Add a {@link FormListener}. These listeners will be called when the form is activated, closed, discarded, before
   * loading, after loading, before storing, after storing, when the structure changes, when it is printed, etc.
   *
   * @param eventTypes
   *          of {@link FormEvent} TYPE_*
   */
  default void addFormListener(FormListener listener, Integer... eventTypes) {
    formListeners().add(listener, false, eventTypes);
  }

  /**
   * Remove a {@link FormListener} that was added to the form before.
   */
  default void removeFormListener(FormListener listener, Integer... eventTypes) {
    formListeners().remove(listener, eventTypes);
  }

  /**
   * Add the listener so it is called as <em>last</em> listener.
   * <p>
   * Use {@link #addFormListener(FormListener, Integer...)}
   *
   * @param eventTypes
   *          of {@link FormEvent} TYPE_*
   */
  default void addUIFormListener(FormListener listener, Integer... eventTypes) {
    formListeners().addLastCalled(listener, false, eventTypes);
  }

  /**
   * @return the {@link IEventHistory} associated with this form
   *         <p>
   *         The default implementation is a {@link DefaultFormEventHistory} and created by
   *         {@link AbstractForm#createEventHistory()}
   *         <p>
   *         This method is thread safe.
   * @since 3.8
   */
  IEventHistory<FormEvent> getEventHistory();

  IFormUIFacade getUIFacade();

}
