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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.ui.swing.extension.ISwingApplicationExtension;

/**
 * <p>
 * Extends the AbstractSwingApplication and provides support for multiple Swing-Scout applications in a single Eclipse
 * application. This class cannot be extended. Use the ISwingApplicationExtension capabilities instead.
 * </p>
 * <p>
 * Use the ranking attribute of the extension point <code>org.eclipse.scout.rt.ui.swing.appextensions</code> to
 * configure the order of the contributed extensions. The extension with the highest ranking is used as
 * default-extension, which provides the swing-environment used to display the splash-screen.
 * </p>
 * 
 * @author awe
 */
final public class ExtensibleSwingApplication extends BaseSwingApplication {

  private static class P_RankedExtension implements Comparable<P_RankedExtension> {

    int ranking;

    ISwingApplicationExtension extension;

    public P_RankedExtension(int ranking, ISwingApplicationExtension extension) {
      this.ranking = ranking;
      this.extension = extension;
    }

    @Override
    public int compareTo(P_RankedExtension o) {
      return o.ranking - ranking;
    }

  }

  private static final String EXTENSION_POINT = Activator.PLUGIN_ID + ".appextensions";

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ExtensibleSwingApplication.class);

  private List<ISwingApplicationExtension> m_extensions = new ArrayList<ISwingApplicationExtension>();

  private ISwingApplicationExtension defaultExtension;

  public ExtensibleSwingApplication() {
    readExtensionPoint();
    initialize();
  }

  /**
   * This constructor should be only used for unit testing.
   * 
   * @param extensions
   */
  ExtensibleSwingApplication(List<ISwingApplicationExtension> extensions) {
    m_extensions = extensions;
  }

  private void readExtensionPoint() {
    List<P_RankedExtension> rankedExtensions = new ArrayList<P_RankedExtension>();
    Set<String> extensionIdSet = new HashSet<String>();

    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint xp = registry.getExtensionPoint(EXTENSION_POINT);
    for (IExtension extension : xp.getExtensions()) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        try {
          ISwingApplicationExtension swingAppExtension = (ISwingApplicationExtension) element.createExecutableExtension("class");
          String extensionId = swingAppExtension.getExtensionId();
          if (extensionIdSet.contains(extensionId)) {
            LOG.error("Already registred a swing application extension with extensionId=" + extensionId + ". ExtensionId must be unique");
          }
          else {
            int ranking = 0;
            String rankingString = element.getAttribute("ranking");
            if (rankingString != null) {
              ranking = Integer.parseInt(rankingString);
            }
            rankedExtensions.add(new P_RankedExtension(ranking, swingAppExtension));
            LOG.debug("Added swing application extension " + swingAppExtension + " (ranking=" + ranking + ")");
          }
        }
        catch (CoreException e) {
          LOG.error("failed to create swing application extension instance. element=" + element, e);
        }
      }
    }
    if (rankedExtensions.isEmpty()) {
      throw new IllegalStateException("no swing application extension contributed! at least one extension is required");
    }
    Collections.sort(rankedExtensions); // order by configured ranking
    LOG.info("Registered " + rankedExtensions.size() + " swing application extensions:");
    for (P_RankedExtension re : rankedExtensions) {
      m_extensions.add(re.extension);
      LOG.info("- " + re.extension);
    }
    defaultExtension = m_extensions.get(0);
    LOG.info("Default swing application extension: " + defaultExtension);
  }

  @Override
  ISwingEnvironment getSwingEnvironment() {
    return defaultExtension.getEnvironment();
  }

  @Override
  void initializeSwing() {
    for (ISwingApplicationExtension ext : m_extensions) {
      ext.initializeSwing();
    }
  }

  @Override
  final protected Object startInSubject(IApplicationContext context) throws Exception {
    for (ISwingApplicationExtension ext : m_extensions) {
      // 1. check if extension wants to exit while start up
      Object exitCode = ext.execStartInSubject(context, getProgressMonitor());
      if (exitCode != null) {
        return exitCode;
      }
      // 2. check if extension has an active (valid) client session
      if (!isClientSessionValid(ext.getClientSession())) {
        return EXIT_OK;
      }
    }
    // Post-condition: session is active and loaded
    context.applicationRunning();
    stopSplashScreen();
    try {
      SwingUtilities.invokeAndWait(
          new Runnable() {
            @Override
            public void run() {
              startGUI();
            }
          }
          );
    }
    catch (Exception e) {
      LOG.warn("Error starting GUI", e);
      System.exit(0);
    }
    return runWhileActive();
  }

  void startGUI() {
    for (ISwingApplicationExtension ext : m_extensions) {
      ext.getEnvironment().showGUI(ext.getClientSession());
    }
  }

  int runWhileActive() throws InterruptedException {
    int exitCode = IApplication.EXIT_OK;
    while (true) {
      // 1. find an active client session
      ISwingApplicationExtension activeExtension = null;
      for (ISwingApplicationExtension ext : m_extensions) {
        if (ext.getClientSession().isActive()) {
          activeExtension = ext;
          break;
        }
      }

      // 2. all extensions terminated (= inactive)? -> terminate
      if (activeExtension == null) {
        return exitCode;
      }

      // 3. wait until a currently active session becomes inactive
      IClientSession clientSession = activeExtension.getClientSession();
      synchronized (clientSession.getStateLock()) {
        if (clientSession.isActive()) {
          clientSession.getStateLock().wait();
          exitCode = clientSession.getExitCode();
        }
      }
    }
  }

  @Override
  public Object start(IApplicationContext context) throws Exception {
    for (ISwingApplicationExtension ext : m_extensions) {
      Object exitCode = ext.execStart(context, getProgressMonitor());
      if (exitCode != null) {
        return exitCode;
      }
    }
    return super.start(context);
  }

  @Override
  public void stop() {
    for (ISwingApplicationExtension ext : m_extensions) {
      ext.getClientSession().stopSession();
    }
  }

}
