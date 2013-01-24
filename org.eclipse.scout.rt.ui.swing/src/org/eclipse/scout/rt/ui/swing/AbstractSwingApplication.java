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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.scout.commons.LocaleUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.prefs.UserScope;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.exceptionhandler.ErrorHandler;
import org.eclipse.scout.rt.client.services.common.exceptionhandler.UserInterruptedException;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.ui.swing.ext.job.SwingProgressHandler;
import org.eclipse.scout.rt.ui.swing.splash.SplashProgressMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;

public abstract class AbstractSwingApplication implements IApplication {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSwingApplication.class);

  private ISwingEnvironment m_env;
  private SplashProgressMonitor m_monitor;
  private ServiceRegistration m_monitorReg;

  public AbstractSwingApplication() {
    if (!Platform.inDevelopmentMode()) {
      try {
        URL instArea = Platform.getInstanceLocation().getURL();
        File sysoutRedirector = new File(instArea.getFile(), System.getProperty("user.name", "anonymous") + "-sysout.log");
        File syserrRedirector = new File(instArea.getFile(), System.getProperty("user.name", "anonymous") + "-syserr.log");
        sysoutRedirector.getParentFile().mkdirs();
        syserrRedirector.getParentFile().mkdirs();
        System.setOut(new PrintStream(new FileOutputStream(sysoutRedirector, true)));
        System.setErr(new PrintStream(new FileOutputStream(syserrRedirector, true)));
      }
      catch (Throwable t) {
        // nop
      }
    }
    // check jre>=1.6.0
    Matcher m = Pattern.compile("([0-9]+\\.[0-9]+\\.[0-9]+)(_.*)?").matcher(System.getProperty("java.version", "0.0.0_00"));
    if (m.matches() && new Version(m.group(1)).compareTo(new Version(1, 6, 0)) < 0) {
      System.out.println("Swing requires at least Java 1.6.0 Current is " + m.group(1));
      System.exit(-1);
    }
    try {
      // The default @{link Locale} has to be set prior to SwingEnvironment is created, because UIDefaultsInjector resolves NLS texts.
      execInitLocale();

      //attach default job handler
      SwingProgressHandler.install();

      SwingUtilities.invokeAndWait(
          new Runnable() {
            @Override
            public void run() {
              m_env = createSwingEnvironment();
            }
          }
          );
    }
    catch (Exception e) {
      LOG.warn(null, e);
      System.exit(0);
    }
    m_monitor = new SplashProgressMonitor(m_env, showSplashScreenProgressInPercentage());
    // register progress as osgi service
    if (Platform.getProduct() != null && Platform.getProduct().getDefiningBundle() != null) {
      BundleContext ctx = Platform.getProduct().getDefiningBundle().getBundleContext();
      m_monitorReg = ctx.registerService(IProgressMonitor.class.getName(), m_monitor, new Hashtable<String, Object>());
    }
    m_monitor.showSplash();
  }

  protected ISwingEnvironment createSwingEnvironment() {
    return new DefaultSwingEnvironment();
  }

  /**
   * @return Returns <code>true</code> if the splash screen shows the current status in percentage as well (if required
   *         data is available). Default is <code>false</code>.
   */
  protected boolean showSplashScreenProgressInPercentage() {
    return false;
  }

  protected abstract IClientSession getClientSession();

  public ISwingEnvironment getSwingEnvironment() {
    return m_env;
  }

  public final IProgressMonitor getProgressMonitor() {
    return m_monitor;
  }

  /**
   * This abstract template application creates a JAAS subject based on the system property "user.name"
   * and supports for initializing the {@link Locale} in {@link #execInitLocale()}
   * <p>
   * The start is then delegated to {@link #startInSubject(IApplicationContext)}
   * <p>
   * Normally {@link #startInSubject(IApplicationContext)} is overriden
   */
  @Override
  public Object start(final IApplicationContext context) throws Exception {
    if (Subject.getSubject(AccessController.getContext()) != null) {
      //there is a subject context
      return exit(startInSubject(context));
    }
    else {
      Subject subject = new Subject();
      subject.getPrincipals().add(new SimplePrincipal(System.getProperty("user.name")));
      return Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
        @Override
        public Object run() throws Exception {
          return exit(startInSubject(context));
        }
      });
    }
  }

  /**
   * Exit delegate to handle os-specific exit behaviour.
   * <p>
   * Mac OS X normally only closes the window, but we want to close the app (with Quit).
   */
  protected Object exit(Object code) {
    if (Platform.OS_MACOSX.equals(Platform.getOS())) {
      System.exit(0);
    }
    return code;
  }

  protected void execInitLocale() {
    Locale locale = LocaleUtility.parse(new UserScope().getNode(org.eclipse.scout.rt.client.Activator.PLUGIN_ID).get("locale", null));
    if (locale != null) {
      Locale.setDefault(locale);
    }
  }

  protected UserAgent initUserAgent() {
    return UserAgent.create(UiLayer.SWING, UiDeviceType.DESKTOP);
  }

  protected Object startInSubject(IApplicationContext context) throws Exception {
    final IClientSession clientSession = getClientSession();
    if (!clientSession.isActive()) {
      showLoadError(clientSession.getLoadError());
      return EXIT_OK;
    }
    // Postcondition: session is active and loaded
    context.applicationRunning();
    if (m_monitorReg != null) {
      m_monitorReg.unregister();
    }
    m_monitor.done();
    m_monitor = null;
    try {
      SwingUtilities.invokeAndWait(
          new Runnable() {
            @Override
            public void run() {
              //start gui
              m_env.showGUI(clientSession);
              execSwingStarted(clientSession);
            }
          }
          );
    }
    catch (Exception e) {
      LOG.warn(null, e);
      System.exit(0);
    }
    while (true) {
      synchronized (clientSession.getStateLock()) {
        if (clientSession.isActive()) {
          clientSession.getStateLock().wait();
        }
        else {
          return clientSession.getExitCode();
        }
      }
    }
  }

  protected void showLoadError(Throwable error) {
    ErrorHandler handler = new ErrorHandler(error);
    if (!(handler.getText().indexOf(UserInterruptedException.class.getSimpleName()) >= 0)) {
      SwingUtility.showMessageDialogSynthCapable(
          null,
          StringUtility.join("\n\n", handler.getText(), handler.getDetail()),
          handler.getTitle(),
          JOptionPane.ERROR_MESSAGE
          );
    }
  }

  /**
   * Called just after the application model was created and is showing in the gui .
   * <p>
   * This method is called in the swing thread.
   */
  protected void execSwingStarted(IClientSession clientSession) {
  }

  @Override
  public void stop() {
    getClientSession().stopSession();
  }

}
