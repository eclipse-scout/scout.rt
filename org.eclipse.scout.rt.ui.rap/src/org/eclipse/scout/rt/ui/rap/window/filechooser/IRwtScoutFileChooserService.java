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
package org.eclipse.scout.rt.ui.rap.window.filechooser;

import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.service.IService2;
import org.eclipse.swt.widgets.Shell;

public interface IRwtScoutFileChooserService extends IService2 {

  IRwtScoutFileChooser createFileChooser(Shell parentShell, IFileChooser fileChooser);
}
