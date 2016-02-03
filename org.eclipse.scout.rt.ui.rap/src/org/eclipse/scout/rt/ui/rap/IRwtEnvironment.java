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
package org.eclipse.scout.rt.ui.rap;

import java.util.Collection;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IUrlTarget;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.servicetunnel.DefaultServiceTunnelContentHandler;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutHtmlValidator;
import org.eclipse.scout.rt.ui.rap.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.html.HtmlAdapter;
import org.eclipse.scout.rt.ui.rap.keystroke.IKeyStrokeManager;
import org.eclipse.scout.rt.ui.rap.keystroke.IRwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.ScoutFormToolkit;
import org.eclipse.scout.rt.ui.rap.window.IRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutFormFooter;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutFormHeader;
import org.eclipse.swt.SWT;
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

/**
 * In order to use rap with a scout application simply setup securtity as follows:
 * <ol>
 * <li>Add a servlet security filter to the /rap client webapp to give the user a rap client Subject</li>
 * <li>Optional:
 * {@link DefaultServiceTunnelContentHandler#createDefaultWsSecurityElement(org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest)}
 * creates a default ws security element for this client subject and sends it with every soap request</li>
 * <li>Add a servlet security filter to the /ajax server webapp to detect the WSSE security token and create a user
 * principal as well as a VirtualSessioIdPrincipal. A default filter is in scout server
 * DefaultVirtualSessionSecurityFilter</li>
 * </ol>
 */
public interface IRwtEnvironment {

  /**
   * key for {@link IClientSession#setData(String, Object)} holding the environment
   */
  String ENVIRONMENT_KEY = "ui.environment";

  /**
   * @param modalities
   *          combination of {@link SWT#SYSTEM_MODAL}, {@link SWT#APPLICATION_MODAL}, {@link SWT#MODELESS}
   * @return best effort to get the "current" parent shell. Never null. ticket
   *         79624
   */
  Shell getParentShellIgnoringPopups(int modalities);

  boolean isInitialized();

  /**
   * Must be called in display thread
   */
  void ensureInitialized();

  Image getIcon(String name);

  ImageDescriptor getImageDescriptor(String iconId);

  ScoutFormToolkit getFormToolkit();

  /**
   * Called from scout job/thread to post an immediate rwt job into the waiting queue.
   * <p>
   * These jobs are run when calling {@link #dispatchImmediateUiJobs()}. Normally this kind of code is only used to
   * early apply visible and enabled properties in {@link RwtScoutFieldComposite#handleUiInputVerifier()} in order to
   * have before-focus-traversal visible/enabled state-update
   */
  void postImmediateUiJob(Runnable r);

  void dispatchImmediateUiJobs();

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
  void addGlobalKeyStroke(IRwtKeyStroke stroke, boolean exclusive);

  /**
   * global key strokes will be executed when and only when no key stroke of the
   * control hierarchy starting at the event's source control consumed
   * (event.doit = false) the event.
   *
   * @param stroke
   * @return
   */
  boolean removeGlobalKeyStroke(IRwtKeyStroke stroke);

  /**
   * @see IKeyStrokeManager#addKeyStroke(Widget, IRwtKeyStroke)
   */
  void addKeyStroke(Control control, IRwtKeyStroke stroke, boolean exclusive);

  /**
   * @see IKeyStrokeManager#removeKeyStroke(Widget, IRwtKeyStroke)
   */
  boolean removeKeyStroke(Control control, IRwtKeyStroke stroke);

  /**
   * @see IKeyStrokeManager#removeKeyStrokes(Widget)
   */
  boolean removeKeyStrokes(Control control);

  /**
   * @see IKeyStrokeManager#hasKeyStroke(Widget, IRwtKeyStroke)
   * @since 3.10.0-M3
   */
  boolean hasKeyStroke(Control control, IRwtKeyStroke stroke);

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

  IDesktop getScoutDesktop();

  // Gui factory
  IRwtScoutForm createForm(Composite parent, IForm scoutForm);

  IRwtScoutFormHeader createFormHeader(Composite parent, IForm scoutForm);

  IRwtScoutFormFooter createFormFooter(Composite parent, IForm scoutForm);

  IRwtScoutFormField createFormField(Composite parent, IFormField model);

  void checkThread();

  void ensureFormPartVisible(IForm form);

  void hideFormPart(IForm form);

  void showFormPart(IForm form);

  Collection<IRwtScoutPart> getOpenFormParts();

  void showFileChooserFromScout(IFileChooser fileChooser);

  /**
   * Open a browser window with some url or address.
   * <p>
   * If the address is a file path, this will trigger a file download from the browser.
   */
  void openBrowserWindowFromScout(String path, IUrlTarget target);

  void showMessageBoxFromScout(IMessageBox messageBox);

  void setClipboardText(String text);

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
   * Convenience for {@link HtmlAdapter#adaptHtmlCell(IRwtScoutComposite, String)}.
   */
  String adaptHtmlCell(IRwtScoutComposite<?> uiComposite, String rawHtml);

  /**
   * Convenience for {@link HtmlAdapter#convertLinksInHtmlCell(IRwtScoutComposite, String)}
   */
  String convertLinksInHtmlCell(IRwtScoutComposite<?> uiComposite, String rawHtml);

  /**
   * Convenience for {@link HtmlAdapter#convertLinksInHtmlCell(IRwtScoutComposite, String, Map)}
   */
  String convertLinksInHtmlCell(IRwtScoutComposite<?> uiComposite, String rawHtml, Map<String, String> params);

  /**
   * Convenience for {@link HtmlAdapter#styleHtmlText(IRwtScoutFormField, String)}.
   */
  String styleHtmlText(IRwtScoutFormField<?> uiComposite, String rawHtml);

  HtmlAdapter getHtmlAdapter();

  /**
   * Returns the display which this environment and the associated UIThread belongs to.
   *
   * @return the {@link Display} for this environment; is never <code>null</code>.
   */
  Display getDisplay();

  IClientSession getClientSession();

  LayoutValidateManager getLayoutValidateManager();

  /**
   * calling from swt thread
   * <p>
   * The job is only run when it reaches the model within the cancelTimeout. This means if the job is delayed longer
   * than cancelTimeout millis when the model job runs it, then the job is ignored.
   *
   * @return the created and scheduled job, a {@link ClientJob}
   */
  JobEx invokeScoutLater(Runnable job, long cancelTimeout);

  void invokeUiLater(Runnable job);

  void addEnvironmentListener(IRwtEnvironmentListener listener);

  void removeEnvironmentListener(IRwtEnvironmentListener listener);

  IRwtScoutHtmlValidator getHtmlValidator();

}
