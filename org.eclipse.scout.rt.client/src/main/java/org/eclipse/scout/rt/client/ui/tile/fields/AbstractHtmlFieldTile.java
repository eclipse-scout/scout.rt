/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.tile.fields;

import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.tile.fields.AbstractHtmlFieldTile.HtmlField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;

/**
 * @since 5.2
 */
@ClassId("acffe2ec-3949-40dc-a5b5-cbe1a94fc934")
public abstract class AbstractHtmlFieldTile extends AbstractFormFieldTile<HtmlField> {

  public AbstractHtmlFieldTile() {
    this(true);
  }

  public AbstractHtmlFieldTile(boolean callInitializer) {
    super(callInitializer);
  }

  @ConfigOperation
  @Order(190)
  protected void execAppLinkAction(String ref) {
  }

  @Order(10)
  @ClassId("fce0eea7-14b9-447f-83fb-0dfbcb2a327e")
  public class HtmlField extends AbstractHtmlField {

    @Override
    public String classId() {
      return AbstractHtmlFieldTile.this.classId() + ID_CONCAT_SYMBOL + ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), true);
    }

    @Override
    protected void execAppLinkAction(String ref) {
      AbstractHtmlFieldTile.this.execAppLinkAction(ref);
    }
  }
}
