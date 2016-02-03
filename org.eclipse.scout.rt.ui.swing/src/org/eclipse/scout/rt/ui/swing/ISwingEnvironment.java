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
package org.eclipse.scout.rt.ui.swing;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIDefaults;

import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionFilter;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.ui.swing.action.ISwingScoutAction;
import org.eclipse.scout.rt.ui.swing.basic.ISwingScoutHtmlValidator;
import org.eclipse.scout.rt.ui.swing.basic.table.ISwingScoutTable;
import org.eclipse.scout.rt.ui.swing.basic.table.SwingTableColumn;
import org.eclipse.scout.rt.ui.swing.ext.JDialogEx;
import org.eclipse.scout.rt.ui.swing.ext.JFrameEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.form.ISwingScoutForm;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.eclipse.scout.rt.ui.swing.form.fields.OnFieldLabelDecorator;
import org.eclipse.scout.rt.ui.swing.form.fields.tabbox.ISwingScoutTabItem;
import org.eclipse.scout.rt.ui.swing.icons.CheckboxIcon;
import org.eclipse.scout.rt.ui.swing.window.ISwingScoutView;
import org.eclipse.scout.rt.ui.swing.window.desktop.ISwingScoutDesktop;
import org.eclipse.scout.rt.ui.swing.window.desktop.ISwingScoutRootFrame;
import org.eclipse.scout.rt.ui.swing.window.desktop.tray.ISwingScoutTray;
import org.eclipse.scout.rt.ui.swing.window.filechooser.ISwingScoutFileChooser;
import org.eclipse.scout.rt.ui.swing.window.messagebox.ISwingScoutMessageBox;

/**
 * All methods of this class run in the swing thread Make sure that all calls to
 * methods of this interface also run in swing thread
 */
public interface ISwingEnvironment {

  /**
   * This method should be called prior to using any methods in the environment
   * After the call to this method, {@link #getRootFrame()} returns the root
   * frame that can be used for splash and later desktop creation. It is
   * encouraged to use only this root frame as the root, so the taskbar will
   * always show just one icon.
   */
  void init();

  /**
   * @return the width in pixel of one logical column (exclusive horizontal gap)
   */
  int getFormColumnWidth();

  /**
   * @return the width in pixel of the horizontal gap betweebn two columns
   */
  int getFormColumnGap();

  /**
   * @return the height in pixel of one logical row (exclusive vertical gap)
   */
  int getFormRowHeight();

  /**
   * @return the height in pixel of the vertical gap between two rows
   */
  int getFormRowGap();

  /**
   * @return the width of the label of an item. This is to support for well aligned forms with equal label sizes.
   */
  int getFieldLabelWidth();

  /**
   * @return the height of a process button (in the bottom part of a group box)
   *         <p>
   *         see {@link org.eclipse.scout.rt.client.ui.form.fields.button.IButton#isProcessButton()
   *         IButton#isProcessButton()}
   *         <p>
   *         by default {@link org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton AbstractOkButton},
   *         {@link org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton AbstractCancelButton},
   *         {@link org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton AbstractCloseButton} etc. are
   *         process buttons
   */
  int getProcessButtonHeight();

  /**
   * @return the size (width, height) in pixel of a button that contains an icon
   */
  int getIconButtonSize();

  /**
   * @return the width of the drop down menu area (the down caret) for a button that contains drop down menus
   */
  int getDropDownButtonWidth();

  Icon getIcon(String name);

  Image getImage(String name);

  /**
   * Customize the ui defaults table.
   * <p>
   * Default entries include:
   *
   * <pre>
   * <li><b>ActivityMap, Calendar</b>
   * Table.focusCellForeground (Color)
   * List.selectionBackground (Color)</li>
   * <li><b>CheckBox disabled fg, GroupBox label disabled fg, TabItem</b>
   * textInactiveText (Color)</li>
   * <li><b>Button enabled/disabled mixer, ToggleButton enabled/disabled mixer, Tree enabled/disabled mixer</b>
   * control (Color)</li>
   * <li><b>LinkButton</b>
   * Hyperlink.foreground (Color)</li>
   * <li><b>GroupBox with title</b>
   * TitledBorder.font (Font)
   * TitledBorder.border (Border)
   * TitledBorder.titleColor (Color)</li>
   * <li><b>HtmlField, MailField</b>
   * Label.font (Font)</li>
   * <li><b>ListBox</b>
   * ListBox.rowHeight (int)</li>
   * <li><b>TabItem custom label/icon</b>
   * TabbedPane.tabAreaInsets (Insets)</li>
   * <li><b>SplashScreen</b>
   * Splash.icon (Icon)
   * Splash.text (Color)</li>
   * <li><b>Status/Progress</b>
   * StatusBar.StopButton.icon (Icon)
   * StatusBar.icon (Icon)
   * StatusBar.height (int)</li>
   * <li><b>RootFrame background</b>
   * desktop (Color)</li>
   * <li><b>MessageBox</b>
   * SystemButton.yesIcon (Icon)
   * SystemButton.noIcon (Icon)
   * SystemButton.cancelIcon (Icon)</li>
   * <li><b>PopupMenu custom border</b>
   * PopupMenu.innerBorder (Border)</li>
   * <li><b>LayoutUtility calculcating label baseline using inset.top</b>
   * TextField.border (Border)</li>
   * <li><b>LayoutUtility</b>
   * Label.defaultHorizontalAlignment (String)
   * </pre>
   */
  void interceptUIDefaults(UIDefaults defaults);

  /**
   * start up the gui
   */
  void showGUI(IClientSession session);

  ISwingScoutRootFrame getRootComposite();

  ISwingScoutTray getTrayComposite();

  Frame getRootFrame();

  void activateStandaloneForm(IForm f);

  void addPropertyChangeListener(PropertyChangeListener listener);

  void removePropertyChangeListener(PropertyChangeListener listener);

  IClientSession getScoutSession();

  IFormField findFocusOwnerField();

  /**
   * This method may be called from showGUI
   */
  Frame createRootFrame();

  /**
   * This method may be called from showGUI
   */
  ISwingScoutRootFrame createRootComposite(Frame rootFrame, IDesktop desktop);

  /**
   * This method may be called from showGUI and/or {@link #createDesktopFrame(IDesktop)}
   */
  ISwingScoutDesktop createDesktop(Window owner, IDesktop desktop);

  void showStandaloneForm(Component parent, IForm model);

  void hideStandaloneForm(IForm f);

  ISwingScoutForm getStandaloneFormComposite(IForm f);

  ISwingScoutForm[] getStandaloneFormComposites();

  void showMessageBox(Component parent, IMessageBox mb);

  ISwingScoutMessageBox createMessageBox(Window w, IMessageBox mb);

  void showFileChooser(Component parent, IFileChooser fc);

  ISwingScoutFileChooser createFileChooser(Window w, IFileChooser fc);

  /**
   * @param owner
   *          optional owner of the dialog
   */
  ISwingScoutView createDialog(Window owner, IForm form);

  ISwingScoutView createPopupDialog(Window parentWindow, IForm form);

  ISwingScoutView createPopupWindow(Window parentWindow, IForm form);

  /**
   * @param owner
   *          optional owner of the frame
   */
  ISwingScoutView createFrame(Window owner, IForm form);

  ISwingScoutView createView(Object viewLayoutConstraints, IForm form);

  /**
   * see {@link #getViewLayoutConstraintsFor(String)}
   */
  Object getViewLayoutConstraintsFor(IForm f);

  /**
   * @return layout constraints that describe how the view is placed over the
   *         3x3 cell martix of the desktop
   */
  Object getViewLayoutConstraintsFor(String viewId);

  /**
   * @param model
   * @param parent
   *          swing parent component for convenience, Note: do not add your
   *          implementation to the parent! this is done by the framework
   */
  ISwingScoutForm createForm(JComponent parent, IForm model);

  ISwingScoutForm createForm(ISwingScoutView targetViewComposite, IForm model);

  /**
   * @param model
   * @param parent
   *          swing parent component for convenience, Note: do not add your
   *          implementation to the parent! this is done by the framework
   */
  ISwingScoutFormField createFormField(JComponent parent, IFormField model);

  ISwingScoutTabItem createTabItem(JComponent parent, IGroupBox field);

  /**
   * create a gui for a list of action, takes care of duplicate, leading and trailing separator handling and
   * recursively creates and attaches child actions on {@link org.eclipse.scout.rt.client.ui.action.tree.IActionNode
   * IActionNode}s and menus
   *
   * @param parent
   *          must not be null, typically a {@link javax.swing.JPopupMenu JPopupMenu}, a {@link javax.swing.JMenu JMenu}
   *          or a {@link javax.swing.JMenuBar JMenuBar}
   */
  void appendActions(JComponent parent, List<? extends IAction> actions, IActionFilter filter);

  /**
   * create a gui for an action, recursively creates and attaches child actions on
   * {@link org.eclipse.scout.rt.client.ui.action.tree.IActionNode IActionNode}s and menus
   */
  ISwingScoutAction createAction(JComponent parent, IAction action, IActionFilter filter);

  /**
   * Called from scout job/thread to post an immediate swing job into the waiting queue.
   * <p>
   * These jobs are run when calling {@link #dispatchImmediateSwingJobs()}. Normally this kind of code is only used to
   * early apply visible and enabled properties in
   * {@link org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite#handleSwingInputVerifier()
   * SwingScoutFieldComposite#handleSwingInputVerifier()} in order to have before-focus-traversal visible/enabled
   * state-update
   */
  void postImmediateSwingJob(Runnable r);

  void dispatchImmediateSwingJobs();

  /**
   * calling from swing thread
   * <p>
   * The job is only run when it reaches the model within the cancelTimeout. This means if the job is delayed longer
   * than cancelTimeout millis when the model job runs it, then the job is ignored.
   *
   * @return the created and scheduled job, a {@link org.eclipse.scout.rt.client.ClientJob ClientJob}
   */
  JobEx invokeScoutLater(Runnable j, long cancelTimeout);

  /**
   * calling from scout thread
   */
  void invokeSwingLater(Runnable j);

  /**
   * calling from scout thread
   * <p>
   * Executes the given {@link Runnable} and waits until it has finished.<br>
   * If the waiting thread is interrupted, this method returns before the {@link Runnable} has finished!
   *
   * @param r
   *          The {@link Runnable} to execute.
   * @param timeout
   *          The timeout in milliseconds. See {@link JobEx#join(long)}.
   */
  void invokeSwingAndWait(final Runnable r, long timeout);

  JStatusLabelEx createStatusLabel(IFormField formField);

  OnFieldLabelDecorator createOnFieldLabelDecorator(JComponent c, boolean mandatory);

  /**
   * Creates the logo of the application. May return a simple JLabel with an icon or an animation.
   * The default impl. creates a JLabel and uses the icon with the ID "logo".
   *
   * @return
   */
  JComponent createLogo();

  /**
   * @return the popupOwner for the (next) popup that is displayed
   */
  Component getPopupOwner();

  /**
   * @return the popupOwnerBounds used for the (next) popup that is displayed
   */
  Rectangle getPopupOwnerBounds();

  void setPopupOwner(Component owner, Rectangle ownerBounds);

  /**
   * Use this decorator to complete and style (incomplete) client html text for html and label fields in order to match
   * current style sheet settings based on a {@link component}s font and color.
   */
  String styleHtmlText(ISwingScoutFormField<?> uiComposite, String rawHtml);

  /**
   * Enables customization of JDialogEx by returning subtypes.
   *
   * @return
   */
  JDialogEx createJDialogEx(Dialog swingParent);

  /**
   * Enables customization of JDialogEx by returning subtypes.
   *
   * @return
   */
  JDialogEx createJDialogEx(Frame swingParent);

  /**
   * Enables customization of JFrameE by returning subtypes.
   *
   * @return
   */
  JFrameEx createJFrameEx();

  /**
   * Creates a swing scout table instance for the given table model. The default implementation returns a
   * SwingScoutTable instance.
   *
   * @param table
   *          Table model
   * @return
   * @since 3.9.0
   */
  ISwingScoutTable createTable(ITable table);

  /**
   * Creates a swing scout table column instance for the given column model. The default implementation returns a
   * SwingTableColumn instance.
   *
   * @param swingModelIndex
   *          modelIndex used to create the swing {@link javax.swing.table.TableColumn TableColumn}
   * @param scoutColumn
   *          the corresponding scout table column
   * @return
   * @since 3.9.0
   */
  SwingTableColumn createColumn(int swingModelIndex, IColumn scoutColumn);

  /**
   * Creates the checkbox Icon used to display boolean values in a Scout table. The default implementation returns a
   * <code>CheckboxWithMarginIcon</code>.
   *
   * @param insets
   *          insets applied on the icon
   * @return a checkbox Icon
   * @since 3.10.0-M3
   */
  CheckboxIcon createCheckboxWithMarginIcon(Insets insets);

  ISwingScoutHtmlValidator getHtmlValidator();

}
