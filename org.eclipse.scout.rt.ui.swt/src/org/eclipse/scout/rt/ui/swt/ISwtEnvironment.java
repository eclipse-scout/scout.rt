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
package org.eclipse.scout.rt.ui.swt;

import java.beans.PropertyChangeListener;
import java.util.Collection;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.swt.busy.SwtBusyHandler;
import org.eclipse.scout.rt.ui.swt.form.ISwtScoutForm;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStrokeFilter;
import org.eclipse.scout.rt.ui.swt.util.ScoutFormToolkit;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.scout.rt.ui.swt.window.desktop.editor.AbstractScoutEditorPart;
import org.eclipse.scout.rt.ui.swt.window.desktop.tray.ISwtScoutTray;
import org.eclipse.scout.rt.ui.swt.window.desktop.view.AbstractScoutView;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;

public interface ISwtEnvironment {

  int ICON_DECORATION_NONE = 0;
  int ICON_DECORATION_EDITABLE_CELL = 1;

  // ui properties
  String PROP_BUTTON_HEIGHT = "Button.height";
  String PROP_ACTIVATION_BUTTON_WIDTH = "ActivationButton.width";
  String PROP_ACTIVATION_BUTTON_HEIGHT = "ActivationButton.height";
  String PROP_ACTIVATION_BUTTON_WIDH_MENU_WIDTH = "ActivationButtonWithMenu.width";
  String PROP_FIELD_LABEL_WIDTH = "FieldLabel.width";
  String PROP_GROUP_BOX_ROW_HEIGHT = "GroupBoxRow.height";
  String PROP_GROUP_BOX_COLUMN_WIDTH = "GroupBoxColumn.width";
  String PROP_GROUP_BOX_HORIZONTAL_SPACING = "GroupBox.horizontalSpacing";
  String PROP_GROUP_BOX_VERTICAL_SPACING = "GroupBox.verticalSpacing";
  String PROP_DIALOG_MIN_HEIGHT = "Dialog.minHeight";
  String PROP_DIALOG_MIN_WIDTH = "Dialog.minWidth";
  String PROP_FORM_FIELD_HORIZONTAL_SPACING = "FormField.horizontalSpacing";

  String PROP_GROUP_BOX_LAYOUT_FIELD_MIN_WIDTH = "GroupBoxLayout.FieldMinWidth";
  String PROP_DISABLED_FOREGROUND_COLOR = "FormField.diabledForegroundColor";

  /**
   * {@link Boolean} busy/idle handling Use positive edge from swt 0->1 and
   * negative edge from scout 1->0
   * 
   * @deprecated replaced by {@link SwtBusyHandler}. Will be removed in Release 3.10.
   */
  @Deprecated
  String PROP_BUSY = "busy";

  Display getDisplay();

  /**
   * @param modalities
   *          combination of {@link org.eclipse.swt.SWT#SYSTEM_MODAL SWT.SYSTEM_MODAL},
   *          {@link org.eclipse.swt.SWT#APPLICATION_MODAL SWT.APPLICATION_MODAL}, {@link org.eclipse.swt.SWT#MODELESS
   *          SWT.MODELESS}
   * @return best effort to get the "current" parent shell. Never null. ticket
   *         79624
   */
  Shell getParentShellIgnoringPopups(int modalities);

  boolean isInitialized();

  /**
   * Must be called in display thread
   */
  void ensureInitialized();

  /**
   * @deprecated replaced by {@link SwtBusyHandler}. Will be removed in Release 3.10.
   */
  @Deprecated
  boolean isBusy();

  void addPropertyChangeListener(PropertyChangeListener listener);

  void removePropertyChangeListener(PropertyChangeListener listener);

  void addEnvironmentListener(ISwtEnvironmentListener listener);

  void removeEnvironmentListener(ISwtEnvironmentListener listener);

  Image getIcon(String name);

  Image getIcon(String name, int iconDecoration);

  ImageDescriptor getImageDescriptor(String iconId);

  ScoutFormToolkit getFormToolkit();

  IClientSession getClientSession();

  /**
   * calling from swt thread
   * <p>
   * The job is only run when it reaches the model within the cancelTimeout. This means if the job is delayed longer
   * than cancelTimeout millis when the model job runs it, then the job is ignored.
   * 
   * @return the created and scheduled job, a {@link org.eclipse.scout.rt.client.ClientJob ClientJob}
   */
  JobEx invokeScoutLater(Runnable job, long cancelTimeout);

  void invokeSwtLater(Runnable job);

  /**
   * Called from scout job/thread to post an immediate swt job into the waiting queue.
   * <p>
   * These jobs are run when calling {@link #dispatchImmediateSwtJobs()}. Normally this kind of code is only used to
   * early apply visible and enabled properties in
   * {@link org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite#handleSwingInputVerifier()
   * SwtScoutFieldComposite#handleSwingInputVerifier()} in order to have before-focus-traversal visible/enabled
   * state-update
   */
  void postImmediateSwtJob(Runnable r);

  void dispatchImmediateSwtJobs();

  Color getColor(String scoutColor);

  Color getColor(RGB rgb);

  /**
   * global key strokes will be executed when and only when no key stroke of the
   * control hierarchy starting at the event's source control consumed
   * (event.doit = false) the event.
   * 
   * @param stroke
   * @return
   */
  void addGlobalKeyStroke(ISwtKeyStroke stroke);

  /**
   * global key strokes will be executed when and only when no key stroke of the
   * control hierarchy starting at the event's source control consumed
   * (event.doit = false) the event.
   * 
   * @param stroke
   * @return
   */
  boolean removeGlobalKeyStroke(ISwtKeyStroke stroke);

  /**
   * @see org.eclipse.scout.rt.ui.swt.keystroke.IKeyStrokeManager#addKeyStroke(Widget, ISwtKeyStroke)
   *      IKeyStrokeManager#addKeyStroke(Widget, ISwtKeyStroke)
   */
  void addKeyStroke(Widget widget, ISwtKeyStroke stroke);

  /**
   * @see org.eclipse.scout.rt.ui.swt.keystroke.IKeyStrokeManager#removeKeyStroke(Widget, ISwtKeyStroke)
   *      IKeyStrokeManager#removeKeyStroke(Widget, ISwtKeyStroke)
   */
  boolean removeKeyStroke(Widget widget, ISwtKeyStroke stroke);

  /**
   * @see org.eclipse.scout.rt.ui.swt.keystroke.IKeyStrokeManager#addKeyStrokeFilter(Widget, ISwtKeyStrokeFilter)
   *      IKeyStrokeManager#addKeyStrokeFilter(Widget, ISwtKeyStrokeFilter)
   */
  void addKeyStrokeFilter(Widget c, ISwtKeyStrokeFilter filter);

  /**
   * @see org.eclipse.scout.rt.ui.swt.keystroke.IKeyStrokeManager#removeKeyStrokeFilter(Widget, ISwtKeyStrokeFilter)
   *      IKeyStrokeManager#removeKeyStrokeFilter(Widget, ISwtKeyStrokeFilter)
   */
  boolean removeKeyStrokeFilter(Widget c, ISwtKeyStrokeFilter filter);

  /**
   * @return a font based on templateFont with style, name and size from scoutFont (if not null).
   *         The result is cached for re-use. Dispose is done automatically and must not be done by the caller
   */
  Font getFont(FontSpec scoutFont, Font templateFont);

  /**
   * @return a font based on templateFont with different style, name and size (if not null).
   *         The result is cached for re-use. Dispose is done automatically and must not be done by the caller
   */
  Font getFont(Font templateFont, String newName, Integer newStyle, Integer newSize);

  // properties
  // int getPropertyInt(String propertyName);
  //
  // String getPropertyString(String propertyName);
  //
  // Object getProperty(String propertyName);
  //
  // boolean getPropertyBool(String propertyName);

  IDesktop getScoutDesktop();

  void showStandaloneForm(IForm form);

  AbstractScoutView getViewPart(String viewId);

  AbstractScoutEditorPart getEditorPart(IEditorInput editorInput, String editorId);

  String[] getAllPartIds();

  Collection<ISwtScoutPart> getOpenFormParts();

  String getSwtPartIdForScoutPartId(String scoutViewLocation);

  String getScoutPartIdForSwtPartId(String viewId);

  // Gui factory
  ISwtScoutForm createForm(Composite parent, IForm scoutForm);

  ISwtScoutFormField createFormField(Composite parent, IFormField model);

  ISwtScoutTray getTrayComposite();

  void checkThread();

  void ensureStandaloneFormVisible(IForm form);

  void registerPart(String scoutViewLocation, String uiViewId);

  void hideStandaloneForm(IForm form);

  void showFileChooserFromScout(IFileChooser fileChooser);

  void showMessageBoxFromScout(IMessageBox messageBox);

  void unregisterPart(String scoutViewLocation);

  void setClipboardText(String text);

  String getPerspectiveId();

  /**
   * @deprecated replaced by {@link SwtBusyHandler}. Will be removed in Release 3.10.
   */
  @Deprecated
  void setBusyFromSwt(boolean b);

  /**
   * @return the popupOwner for the (next) popup that is displayed
   */
  Control getPopupOwner();

  /**
   * @return the popupOwnerBounds used for the (next) popup that is displayed
   */
  Rectangle getPopupOwnerBounds();

  void setPopupOwner(Control owner, Rectangle ownerBounds);

  /**
   * Sets the image and message to be displayed on the status line of every open {@link ISwtScoutPart}.
   * 
   * @param image
   *          the image to use, or <code>null</code> for no image
   * @param message
   *          the message, or <code>null</code> for no message
   */
  void setStatusLineMessage(Image image, String message);

  /**
   * Use this decorator to complete and style (incomplete) client html text for html and label fields in order to match
   * current style
   * sheet settings based on a {@link Control}s font and color
   */
  String styleHtmlText(ISwtScoutFormField<?> uiComposite, String rawHtml);

  /**
   * @deprecated use {@link IForm#getEventHistory()} Will be removed in Release 3.10.
   */
  @Deprecated
  FormEvent[] fetchPendingPrintEvents(IForm form);

}
