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
package org.eclipse.scout.rt.client.mobile.ui.form;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.9.0
 */
public abstract class AbstractMobileForm extends AbstractForm implements IMobileForm {

  public AbstractMobileForm() {
    this(true);
  }

  public AbstractMobileForm(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    setHeaderVisible(getConfiguredHeaderVisible());
    setFooterVisible(getConfiguredFooterVisible());
    setHeaderActionFetcher(createHeaderActionFetcher());
    setFooterActionFetcher(createFooterActionFetcher());
  }

  protected boolean getConfiguredHeaderVisible() {
    return false;
  }

  protected boolean getConfiguredFooterVisible() {
    return false;
  }

  @Override
  public boolean isHeaderVisible() {
    return isHeaderVisible(this);
  }

  @Override
  public void setHeaderVisible(boolean visible) {
    setHeaderVisible(this, visible);
  }

  public static void setHeaderVisible(IForm form, boolean visible) {
    form.setProperty(PROP_HEADER_VISIBLE, visible);
  }

  public static boolean isHeaderVisible(IForm form) {
    Boolean b = (Boolean) form.getProperty(PROP_HEADER_VISIBLE);
    return b != null ? b.booleanValue() : false;
  }

  @Override
  public boolean isFooterVisible() {
    return isFooterVisible(this);
  }

  @Override
  public void setFooterVisible(boolean visible) {
    setFooterVisible(this, visible);
  }

  public static boolean isFooterVisible(IForm form) {
    Boolean b = (Boolean) form.getProperty(PROP_FOOTER_VISIBLE);
    return b != null ? b.booleanValue() : false;
  }

  public static void setFooterVisible(IForm form, boolean visible) {
    form.setProperty(PROP_FOOTER_VISIBLE, visible);
  }

  protected IActionFetcher createHeaderActionFetcher() {
    return new FormHeaderActionFetcher(this);
  }

  protected IActionFetcher createFooterActionFetcher() {
    return new FormFooterActionFetcher(this);
  }

  public static IActionFetcher getHeaderActionFetcher(IForm form) {
    return (IActionFetcher) form.getProperty(IMobileForm.PROP_HEADER_ACTION_FETCHER);
  }

  public static void setHeaderActionFetcher(IForm form, IActionFetcher headerActionFetcher) {
    form.setProperty(IMobileForm.PROP_HEADER_ACTION_FETCHER, headerActionFetcher);
  }

  @Override
  public IActionFetcher getHeaderActionFetcher() {
    return getHeaderActionFetcher(this);
  }

  @Override
  public void setHeaderActionFetcher(IActionFetcher headerActionFetcher) {
    setHeaderActionFetcher(this, headerActionFetcher);
  }

  @Override
  public IActionFetcher getFooterActionFetcher() {
    return getFooterActionFetcher(this);
  }

  @Override
  public void setFooterActionFetcher(IActionFetcher footerActionFetcher) {
    setFooterActionFetcher(this, footerActionFetcher);
  }

  public static IActionFetcher getFooterActionFetcher(IForm form) {
    return (IActionFetcher) form.getProperty(IMobileForm.PROP_FOOTER_ACTION_FETCHER);
  }

  public static void setFooterActionFetcher(IForm form, IActionFetcher footerActionFetcher) {
    form.setProperty(IMobileForm.PROP_FOOTER_ACTION_FETCHER, footerActionFetcher);
  }

}
