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
package org.eclipse.scout.rt.client.ui.form.fields.mailfield;

import javax.mail.internet.MimeMessage;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

/**
 * @version 3.x
 */
public interface IMailField extends IValueField<MimeMessage> {
  String PROP_LABEL_FROM = "labelFrom";
  String PROP_LABEL_TO = "labelTo";
  String PROP_LABEL_CC = "labelCC";
  String PROP_LABEL_SUBJECT = "labelSubject";

  boolean isMailEditor();

  boolean isScrollBarEnabled();

  String getLabelFrom();

  void setLabelFrom(String fromLabel);

  String getLabelTo();

  void setLabelTo(String toLabel);

  String getLabelCc();

  void setLabelCc(String ccLabel);

  String getLabelSubject();

  void setLabelSubject(String subjectLabel);

  IMailFieldUIFacade getUIFacade();
}
