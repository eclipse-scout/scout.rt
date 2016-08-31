/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.htmlfield;

import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.IAppLinkCapable;
import org.eclipse.scout.rt.client.ui.IHtmlCapable;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

/**
 * With a html field it is possible to display custom html content.
 * <p>
 * Compared to {@link ILabelField} with {@link ILabelField#isHtmlEnabled()} = true it provides some more functionality
 * like scrolling. <br>
 * Compared to the {@link IBrowserField}, the content is embedded directly into the main html document without using an
 * iframe.
 *
 * @see IBrowserField
 * @see ILabelField
 */
public interface IHtmlField extends IValueField<String>, IAppLinkCapable, IHtmlCapable {

  String PROP_SCROLL_BAR_ENABLED = "scrollBarEnabled";
  String PROP_SCROLL_TO_END = "scrollToEnd";
  String PROP_SCROLL_TO_ANCHOR = "scrollToAnchor";

  void setScrollToAnchor(String anchorName);

  String getScrollToAnchor();

  void setScrollBarEnabled(boolean scrollBarEnabled);

  boolean isScrollBarEnabled();

  void scrollToEnd();

  IHtmlFieldUIFacade getUIFacade();

  /**
   * local images and local resources bound to the html text
   */
  Set<BinaryResource> getAttachments();

  BinaryResource getAttachment(String filename);

  void setAttachments(Collection<? extends BinaryResource> attachments);

  void addAttachment(BinaryResource attachment);

  void removeAttachment(BinaryResource attachment);

  String getPlainText();
}
