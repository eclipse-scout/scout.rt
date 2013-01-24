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

import java.io.File;
import java.net.URL;

import javax.mail.internet.MimeMessage;

/**
 * @version 3.x
 */
@SuppressWarnings("restriction")
public interface IMailFieldUIFacade {

  boolean setMailFromUI(MimeMessage message);

  void fireAttachementActionFromUI(File file);

  void fireHyperlinkActionFromUI(URL url);

}
