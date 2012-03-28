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
package org.eclipse.scout.rt.client.ui.form;

import java.security.Permission;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.IPropertyFilter;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.client.ui.wizard.IWizard;
import org.eclipse.scout.rt.client.ui.wizard.IWizardStep;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

/**
 * A form is the model used for anything containing fields: a dialog, a step
 * of a wizard, the details of a page, a search. Each form is represented by
 * a class.
 * A form has fields, buttons, and handlers. These elements
 * are all part of the same class, they are <b>inner classes</b> of the form.
 * <p>
 * A <b>field</b> is where you enter data: a string, a number, a date, a list of values. A <b>button</b> is where you
 * trigger actions: save data, cancel whatever you are doing, go on to the next or return to the previous step. A
 * <b>handler</b> is reponsible for loading from data and storing data. This usually involves calling process services
 * on the server. These will in turn contact a persistence layer such as a database.
 */
public interface IForm extends IPropertyObserver {

  String PROP_TITLE = "title";
  String PROP_MINIMIZE_ENABLED = "minimizeEnabled";
  String PROP_MAXIMIZE_ENABLED = "maximizeEnabled";
  String PROP_MINIMIZED = "minimized";
  String PROP_MAXIMIZED = "maximized";
  String PROP_EMPTY = "empty";
  String PROP_SAVE_NEEDED = "saveNeeded";
  String PROP_ICON_ID = "iconId";
  String PROP_PERSPECTIVE_ID = "perspectiveId";

  /**
   * Standalone window<br>
   * Swing: modal -> JDialog, nonmodal -> JFrame<br>
   * SWT: modal -> Dialog, nonmodal -> Dialog, Wizard Dialog
   */
  int DISPLAY_HINT_DIALOG = 0;
  /**
   * Popup view is a popup window that is automatically closed when mouse clicks outside of popup<br>
   * Swing: JPopup<br>
   * SWT: Window+Shell
   */
  int DISPLAY_HINT_POPUP_WINDOW = 10;
  /**
   * Popup dialog is a normal dialog, but placement is at the focus owner location instead of default dialog position
   * Swing: JPopup<br>
   * SWT: Window+Shell
   */
  int DISPLAY_HINT_POPUP_DIALOG = 12;
  /**
   * Inline view<br>
   * Swing: JInternalFrame<br>
   * SWT: View, Editor, , Wizard Editor
   */
  int DISPLAY_HINT_VIEW = 20;

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

  String EDITOR_ID = "EDITOR";

  /**
   * Initialize the form and all of its fields.
   * By default any of the #start* methods of the form call this method
   */
  void initForm() throws ProcessingException;

  /**
   * This method is called to get an exclusive key of the form. The key is used
   * to open the same form with the same handler only once. Obviously this
   * behavior can only be used for view forms.
   * 
   * @see AbstractDesktop#getSimilarViewForms(IForm)
   * @return null for exclusive form behavior an exclusive key to ensure similar
   *         handling.
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(5)
  Object computeExclusiveKey() throws ProcessingException;

  /*
   * Runtime
   */
  void setEnabledPermission(Permission p);

  /**
   * Activate the form in terms of UI visibility / focus target<br>
   * This will send a desktop event {@link DesktopEvent#TYPE_FORM_ENSURE_VISIBLE}
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

  boolean isEnabledGranted();

  void setEnabledGranted(boolean b);

  void setVisiblePermission(Permission p);

  boolean isVisibleGranted();

  void setVisibleGranted(boolean b);

  String getFormId();

  IFormHandler getHandler();

  void setHandler(IFormHandler handler);

  String getIconId();

  void setIconId(String s);

  String getPerspectiveId();

  void setPerspectiveId(String s);

  /**
   * @return the {@link IWizard} that contains the step that started this form
   *         using startWizardStep
   */
  IWizard getWizard();

  /**
   * @return the {@link IWizardStep} that started this form using
   *         startWizardStep
   */
  IWizardStep getWizardStep();

  /**
   * @param wizardStep
   *          the step that starts this form
   * @param handlerType
   *          the inner handler type used to load / store the form, null when no
   *          automatic load/store should be performed
   */
  void startWizardStep(IWizardStep wizardStep, Class<? extends IFormHandler> handlerType) throws ProcessingException;

  /**
   * @return true if the form is currently attached to the desktop, false if the
   *         form is not attached to the desktop<br>
   *         This method can be used to determine if a possibly active form
   *         (started with a running form handler) is currently showing on the
   *         desktop.
   */
  boolean isShowing();

  /**
   * @return true if the form is not (yet) started with a form handler and
   *         therefore not active
   */
  boolean isFormClosed();

  /**
   * @return true if the form is started with a form handler and therefore
   *         active
   */
  boolean isFormOpen();

  /**
   * true while the {@link IFormHandler#execLoad()} method is running<br>
   * this is often used in {@link AbstractValueField#execChangedValue()}
   */
  boolean isFormLoading();

  /**
   * fill a FormData structure to be sent to the backend<br>
   * the configurator is creating typed subclasses of FormData and FormFieldData
   */
  void exportFormData(AbstractFormData target) throws ProcessingException;

  /**
   * apply FormData to this form
   */
  void importFormData(AbstractFormData source) throws ProcessingException;

  /**
   * apply FormData to this form<br>
   * value triggers can be deactivated if necessary
   */
  void importFormData(AbstractFormData source, boolean valueChangeTriggersEnabled, IPropertyFilter filter) throws ProcessingException;

  /**
   * @return the {@link IFormField} that owns the focus AND is inside this form.<br>
   *         In order to get the global focus owner, use {@link IDesktop#getFocusOwner()}.
   */
  IFormField getFocusOwner();

  /**
   * traverse all fields recursive and return them as a list
   */
  IFormField[] getAllFields();

  List<? extends IFormField> getInvalidFields();

  void validateForm() throws ProcessingException;

  boolean visitFields(IFormFieldVisitor visitor);

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
   * When a form is set as the inner form of a {@link IWrappedFormField}<br>
   * Do not use this internal method directly, it is automatically called by
   * {@link IWrappedFormField#setInnerForm(IForm)}
   */
  void setWrapperFieldInternal(IWrappedFormField w);

  /**
   * the field ID is the simple class name of a field without the suffixes
   * "Box", "Field", "Button"
   */
  IFormField getFieldById(final String id);

  /**
   * the field ID is the simple class name of a field without the suffixes
   * "Box", "Field", "Button"
   * The field must be equal or a subtype of type
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

  void doClose() throws ProcessingException;

  void doCancel() throws ProcessingException;

  /**
   * Save data and close the form.
   */
  void doOk() throws ProcessingException;

  /**
   * Validate the form, save it, and make all fields as saved.
   * The net result is that calling this method again on the unchanged form
   * will revalidate it, but will no longer save it.
   */
  void doSave() throws ProcessingException;

  /**
   * similar to {@link #doSave()} but do NOT set fields to state {@link #isSaveNeeded()}=false
   */
  void doSaveWithoutMarkerChange() throws ProcessingException;

  void doReset();

  void doFinally();

  void doExportXml(boolean saveAs);

  void doImportXml();

  /**
   * Property is true by default.<br>
   * This automatically calls {@link org.eclipse.scout.rt.client.ui.desktop.IDesktop#addForm(IForm)} when
   * the form is started.<br>
   * When using forms inside pages such as
   * {@link org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage#setDetailForm(IForm)} and
   * {@link org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable#getConfiguredSearchForm()} then this
   * flag is set to false, because then the form should not be
   * displayed unless the page it is contained in is activated. This activation
   * will then call {@link org.eclipse.scout.rt.client.ui.desktop.IDesktop#addForm(IForm)}
   */
  boolean isAutoAddRemoveOnDesktop();

  /**
   * @see #isAutoAddRemoveOnDesktop()
   */
  void setAutoAddRemoveOnDesktop(boolean b);

  void setCloseTimer(int seconds);

  void removeCloseTimer();

  void setTimer(String timerId, int seconds);

  void removeTimer(String timerId);

  void setAllEnabled(boolean b);

  String getCancelVerificationText();

  void setCancelVerificationText(String text);

  /**
   * Determine whether a save is needed for the form.
   */
  boolean isSaveNeeded();

  /**
   * this method calls execCheckSaveNeeded on every field<br>
   * to ensure that {@link #isSaveNeeded()} returns the correct value
   */
  void checkSaveNeeded();

  /**
   * Mark the form as saved so that a save is no longer needed.
   */
  void markSaved();

  /**
   * Touch the form so so that a save is needed.
   */
  void touch();

  boolean isEmpty();

  String getBasicTitle();

  void setBasicTitle(String basicTitle);

  String getSubTitle();

  void setSubTitle(String subTitle);

  /**
   * The system button (type) that triggered the save
   * 
   * @see IButton.SYSTEM_TYPE_...
   */
  int getCloseSystemType();

  void setCloseSystemType(int saveType);

  String getTitle();

  void setTitle(String s);

  /**
   * UI hint that gui should be maximized
   */
  boolean isMaximized();

  void setMaximized(boolean b);

  boolean isMaximizeEnabled();

  void setMaximizeEnabled(boolean b);

  /**
   * UI hint that gui should be minimized
   */
  boolean isMinimized();

  void setMinimized(boolean b);

  boolean isMinimizeEnabled();

  void setMinimizeEnabled(boolean b);

  boolean isModal();

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
   * @return one of the DISPLAY_HINT_* constants or a custom value
   */
  int getDisplayHint();

  /**
   * use one ofe the DISPLAY_HINT constants or a custom value
   */
  void setDisplayHint(int i);

  /**
   * @return one of the VIEW_ID_* constants or a custom text
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
   * marker property to signal that the form changed some data (using services,
   * backend)<br>
   * property is normally used after the form handler is returned
   */
  boolean isFormStored();

  /**
   * marker property to signal that the form changed some data (using services,
   * backend)<br>
   * this property is automatically set whenever the form has called the
   * handlers execStore (disregarding whether this was successful or not)
   */
  void setFormStored(boolean b);

  Object getCustomProperty(String propName);

  void setCustomProperty(String propName, Object o);

  boolean hasProperty(String name);

  /**
   * XML export/import of form state
   */
  void setXML(String xml) throws ProcessingException;

  String getXML(String encoding) throws ProcessingException;

  SimpleXmlElement storeXML() throws ProcessingException;

  void storeXML(SimpleXmlElement root) throws ProcessingException;

  void loadXML(SimpleXmlElement root) throws ProcessingException;

  /**
   * Wait until form is closed<br>
   * If the form is modal this method returns just after the modal handler has
   * terminated<br>
   * If the form is non-modal this starts a sub event dispatcher that loops (and
   * blocks) until form handling is false (i.e. form has been closed)
   */
  void waitFor() throws ProcessingException;

  int WAIT_FOR_ERROR_CODE = 69218;

  /**
   * reset, create, validate the search model that contains the form data, verbose search texts and a valid status
   * see {@link SearchFilter#clear()} see {@link #doSaveWithoutMarkerChange()} is also resetting the search model
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
   * Prints the form<br>
   * <p>
   * The method returns immediately, the print is done int the background.
   * <p>
   * For details and parameter details see {@link PrintDevice}
   */
  void printForm(PrintDevice device, Map<String, Object> parameters);

  /**
   * Prints a form field<br>
   * <p>
   * The method returns immediately, the print is done int the background.
   * <p>
   * For details and parameter details see {@link PrintDevice}
   */
  void printField(IFormField field, PrintDevice device, Map<String, Object> parameters);

  /**
   * Request focus for the field by sending a {@link FormEvent#TYPE_REQUEST_FOCUS} event
   */
  void requestFocus(IFormField f);

  /**
   * Add a {@link FormListener}. These listeners will be called
   * when the form is activated, closed, discared, before loading,
   * after loading, before storing, after storing, when the structure changes, when it is
   * printed, etc.
   */
  void addFormListener(FormListener listener);

  /**
   * Remove a {@link FormListener} that was added to the form before.
   */
  void removeFormListener(FormListener listener);

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
