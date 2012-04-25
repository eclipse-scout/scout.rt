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
package org.eclipse.scout.rt.client.services.common.shell;

import java.io.File;
import java.io.IOException;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.shell.IShellService;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;
import org.eclipse.scout.service.AbstractService;

/**
 *
 */
@Priority(0)
public class DefaultShellService extends AbstractService implements IShellService {

  @Override
  public void shellOpen(String path) throws ProcessingException {
    if (UserAgentUtility.isWebClient()) {
      ClientSyncJob.getCurrentSession().getDesktop().openBrowserWindow(path);
    }
    else {
      openWithRuntimeExec(path);
    }
  }

  protected String validatePath(String path) throws IOException {
    path = path.replace('\\', File.separatorChar);
    if (new File(path).exists()) {
      path = new File(path).getCanonicalPath();
      String osName = System.getProperty("os.name");
      if (osName != null && osName.startsWith("Mac OS")) {
        //mac is not able to open files with a space, even when in quotes
        String ext = path.substring(path.lastIndexOf('.'));
        File f = new File(new File(path).getParentFile(), "" + System.nanoTime() + ext);
        new File(path).renameTo(f);
        f.deleteOnExit();
        path = f.getAbsolutePath();
      }
    }
    return path;
  }

  protected void openWithRuntimeExec(String path) throws ProcessingException {
    String osName = System.getProperty("os.name");
    if (osName == null) {
      return;
    }
    try {
      String commandline = null;
      if (osName.startsWith("Windows")) {
        String pathQuoted = "\"" + validatePath(path) + "\"";
        commandline = "cmd.exe /c start \"\" " + pathQuoted;
      }
      else if (osName.startsWith("Mac OS")) {
        //mac is not able to open files with a space, even when in quotes
        commandline = "open " + validatePath(path);
      }
      else if (osName.startsWith("Linux")) {
        String pathQuoted = "\"" + validatePath(path) + "\"";
        commandline = "xdg-open " + pathQuoted;
      }
      if (commandline == null) {
        return;
      }
      System.out.println("EXEC1: " + commandline);
      Process process = Runtime.getRuntime().exec(commandline);
      System.out.println("EXEC2: " + process);
      int code = process.waitFor();
      System.out.println("EXEC3: " + code);
    }
    catch (InterruptedException ie) {
      throw new ProcessingException(ScoutTexts.get("Interrupted"), ie);
    }
    catch (Throwable t) {
      throw new ProcessingException("Unexpected: " + path, t);
    }
  }

}
