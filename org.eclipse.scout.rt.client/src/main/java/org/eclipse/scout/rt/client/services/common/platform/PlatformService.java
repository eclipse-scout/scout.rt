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
package org.eclipse.scout.rt.client.services.common.platform;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.client.ui.basic.filechooser.FileChooser;
import org.eclipse.scout.service.AbstractService;

@Priority(-1)
public class PlatformService extends AbstractService implements IPlatformService {

  @Override
  public String getFile() {
    return getFile(null, true);
  }

  @Override
  public String getFile(String ext, boolean open) {
    return getFile(ext, open, null);
  }

  @Override
  public String getFile(String ext, boolean open, String curPath) {
    return getFile(ext, open, curPath, false);
  }

  @Override
  public String getFile(String ext, boolean open, String curPath, boolean folderMode) {
    if (curPath == null) {
      curPath = FileChooser.getCurrentDirectory();
      if (curPath == null) {
        curPath = ConfigIniUtility.getProperty("user.home");
      }
    }
    File f = null;
    List<File> a = new FileChooser(new File(curPath), Collections.singletonList(ext), open).startChooser();
    if (a.size() > 0) {
      f = a.get(0);
    }
    //
    if (f == null) {
      /* nop */
    }
    else if (f.isDirectory() != folderMode) {
      f = null;
    }
    else if (f.getAbsolutePath().indexOf('*') >= 0) {
      f = null;
    }
    //
    if (f != null) {
      FileChooser.setCurrentDirectory(f.getParent());
      return f.getAbsolutePath();
    }
    return null;
  }

}
