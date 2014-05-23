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
package org.eclipse.scout.rt.ui.html.json;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ui.IUiDeviceType;
import org.eclipse.scout.rt.shared.ui.IUiLayer;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.json.JSONObject;

public abstract class AbstractJsonSession implements IJsonSession, HttpSessionBindingListener {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonSession.class);

  private JsonClientSession m_jsonClientSession;

  // TODO AWE: JsonRendererFactory überschreibbar machen, via Scout-service
  private final JsonRendererFactory m_jsonRendererFactory;

  private final JsonRendererRegistry m_jsonRendererRegistry;

  private long m_jsonRendererSeq;
  private JsonResponse m_currentJsonResponse;
  private HttpServletRequest m_currentHttpRequest;

  public AbstractJsonSession() {
    m_currentJsonResponse = new JsonResponse();
    m_jsonRendererFactory = new JsonRendererFactory();
    m_jsonRendererRegistry = new JsonRendererRegistry();
  }

  @Override
  public void init(HttpServletRequest request, JsonRequest jsonReq) {
    m_currentHttpRequest = request;
    UserAgent userAgent = createUserAgent(jsonReq);
    Subject subject = initSubject();
    if (subject == null) {
      throw new SecurityException("/json request is not authenticated with a Subject");
    }
    IClientSession clientSession = createClientSession(userAgent, subject, request.getLocale());
    // FIXME AWE/CGU: use sessionId or use createUniqueIdFor? duplicates possible?
    // was <<jsonReq.getSessionPartId()>> before, now createUniqueIdFor is used again
    m_jsonClientSession = (JsonClientSession) getOrCreateJsonRenderer(clientSession);
    if (!clientSession.isActive()) {
      throw new JsonException("ClientSession is not active, there must be a problem with loading or starting");
    }
    JSONObject json = initJsonSession(clientSession);
    m_currentJsonResponse.addCreateEvent(jsonReq.getSessionPartId(), json);
    LOG.info("JsonSession initialized");
  }

  /**
   * This call runs in a Scout job and creates the initial JSON session object with the Desktop.
   */
  private JSONObject initJsonSession(IClientSession clientSession) {
    final Holder<JSONObject> jsonHolder = new Holder<>(JSONObject.class);
    new ClientSyncJob("AbstractJsonSession#init", clientSession) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        jsonHolder.setValue(m_jsonClientSession.toJson());
      }
    }.runNow(new NullProgressMonitor());
    return jsonHolder.getValue();
  }

  protected UserAgent createUserAgent(JsonRequest jsonReq) {
    // FIXME CGU create UiLayer.HTML
    IUiLayer uiLayer = UiLayer.RAP;
    IUiDeviceType uiDeviceType = UiDeviceType.DESKTOP;
    String browserId = m_currentHttpRequest.getHeader("User-Agent");
    JSONObject userAgent = jsonReq.getUserAgent();
    if (userAgent != null) {
      // FIXME CGU it would be great if UserAgent could be changed dynamically, to switch from mobile to tablet mode on the fly, should be done as event in JsonClientSession
      String uiDeviceTypeStr = userAgent.optString("deviceType", null);
      if (uiDeviceTypeStr != null) {
        uiDeviceType = UiDeviceType.createByIdentifier(uiDeviceTypeStr);
      }
      String uiLayerStr = userAgent.optString("uiLayer", null);
      if (uiLayerStr != null) {
        uiLayer = UiLayer.createByIdentifier(uiLayerStr);
      }
    }
    return UserAgent.create(uiLayer, uiDeviceType, browserId);
  }

  protected Subject initSubject() {
    return Subject.getSubject(AccessController.getContext());
  }

  protected abstract Class<? extends IClientSession> clientSessionClass();

  protected IClientSession createClientSession(UserAgent userAgent, Subject subject, Locale locale) {
    LocaleThreadLocal.set(locale);
    try {
      return createClientSessionInternal(clientSessionClass(), userAgent, subject, locale, UUID.randomUUID().toString());
      //FIXME CGU session must be started later, see JsonClientSession
      //return SERVICES.getService(IClientSessionRegistryService.class).newClientSession(clientSessionClass(), subject, UUID.randomUUID().toString(), userAgent);
    }
    finally {
      LocaleThreadLocal.set(null);
    }
  }

  private IClientSession createClientSessionInternal(Class<? extends IClientSession> clazz, UserAgent userAgent, Subject subject, Locale locale, String virtualSessionId) {
    try {
      IClientSession clientSession = clazz.newInstance();
      clientSession.setSubject(subject);
      if (virtualSessionId != null) {
        clientSession.setVirtualSessionId(virtualSessionId);
      }
      clientSession.setUserAgent(userAgent);

      return clientSession;
    }
    catch (Throwable t) {
      LOG.error("could not load session", t);
      return null;
    }
  }

  public void dispose() {
    m_jsonRendererRegistry.dispose();
  }

  @Override
  public IClientSession getClientSession() {
    return m_jsonClientSession.getModelObject();
  }

  @Override
  public String createUniqueIdFor(IJsonRenderer jsonRenderer) {
    //FIXME CGU create id based on scout object for automatic gui testing, use @classId? or CustomWidgetIdGenerator from scout.ui.rwt bundle?
    return "" + (++m_jsonRendererSeq);
  }

  @Override
  public IJsonRenderer<?> getOrCreateJsonRenderer(Object modelObject) {
    IJsonRenderer<?> jsonRenderer = getJsonRenderer(modelObject);
    if (jsonRenderer != null) {
      return jsonRenderer;
    }
    jsonRenderer = createJsonRenderer(modelObject);
    return jsonRenderer;
  }

  @Override
  public IJsonRenderer<?> createJsonRenderer(Object modelObject) {
    String id = createUniqueIdFor(null); //FIXME cgu
    IJsonRenderer<?> jsonRenderer = m_jsonRendererFactory.createJsonRenderer(modelObject, this, id);
    jsonRenderer.init();
    m_jsonRendererRegistry.addJsonRenderer(id, modelObject, jsonRenderer);
    return jsonRenderer;
  }

  @Override
  public IJsonRenderer<?> getJsonRenderer(String id) {
    return m_jsonRendererRegistry.getJsonRenderer(id);
  }

  @Override
  public IJsonRenderer<?> getJsonRenderer(Object modelObject) {
    return m_jsonRendererRegistry.getJsonRenderer(modelObject);
  }

  @Override
  public void unregisterJsonRenderer(String id) {
    m_jsonRendererRegistry.removeJsonRenderer(id);
  }

  @Override
  public JsonResponse currentJsonResponse() {
    return m_currentJsonResponse;
  }

  @Override
  public HttpServletRequest currentHttpRequest() {
    return m_currentHttpRequest;
  }

  @Override
  public JsonResponse processRequest(HttpServletRequest httpReq, JsonRequest jsonReq) {
    m_currentHttpRequest = httpReq;

    //FIXME CGU should only be done after pressing reload, maybe on get request? first we need to fix reload bug, see FIXME in AbstractJsonServlet
    m_jsonClientSession.processRequestLocale(httpReq.getLocale());

    final JsonResponse res = currentJsonResponse();

    for (final JsonEvent event : jsonReq.getEvents()) {
      // TODO AWE: (jobs) prüfen ob das hier probleme macht: dadurch läuft processEvent immer im richtigen
      // context. JsonRenderer instanzen müssen somit nicht immer einen ClientSyncJob starten wenn sie z.B.
      // einen Scout-service aufrufen wollen. Es wurde bewusst für jedes processEvent ein eigener Job gestartet
      // und nicht für den ganzen Loop.
      new ClientSyncJob("processEvent", getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          processEvent(event, res);
        }
      }.schedule();
    }

    // see --> ClientJob#rescheduleWaitingSyncJobs
    // wir wollen vermutlich auch nur die Jobs die von unserer client session gestartet worden sind, oder?
    while (true) {
      List<ClientJob> jobList = new ArrayList<>();
      for (Job job : Job.getJobManager().find(ClientJob.class)) {
        if (job instanceof ClientJob) {
          ClientJob clientJob = (ClientJob) job;
          if (clientJob.getClientSession() == getClientSession()) {
            jobList.add(clientJob);
          }
        }
      }

      if (jobList.isEmpty()) {
        LOG.info("Job list is empty. Finish request");
        break;
      }
      else {
        int numJobs = jobList.size();
        int numSync = 0;
        int numWaitFor = 0;
        for (ClientJob job : jobList) {
          LOG.info("--------- JOB:" + job + " sync=" + job.isSync() + " waitFor=" + job.isWaitFor());
          if (job.isWaitFor()) {
            numWaitFor++;
          }
          else if (job.isSync()) {
            numSync++;
          }
        }
        LOG.info("Job list size=" + numJobs + ", sync (running)=" + numSync + " waitFor (blocking)=" + numWaitFor);
        if (numSync > 0) {
          LOG.info("There are still running sync jobs - must wait until they have finished");
        }
        else if (numJobs == numWaitFor) {
          LOG.info("Only 'waitFor' jobs left in the queue - it's allowed to finish the request");
          break;
        }

        // TODO AWE: das geht sicher schöner (notify?)
        // wir könnten es auch mit einem IJobChangeListener versuchen....
        LOG.info("Going to sleep before checking the job queue again...");
        try {
          Thread.sleep(25);
        }
        catch (InterruptedException e) {
          e.printStackTrace(); // FIXME AWE
        }
      }
    }

    //Clear event map when sent to client
    m_currentJsonResponse = new JsonResponse();
    return res;
  }

  protected void processEvent(JsonEvent event, JsonResponse res) {
    final String id = event.getEventId(); // TODO AWE: (ask C.GU) das sollte besser rendererId oder widgetId heissen, nicht?
    final IJsonRenderer jsonRenderer = getJsonRenderer(id);
    if (jsonRenderer == null) {
      throw new JsonException("No renderer found for id " + id);
    }
    try {
      LOG.info("Handling event. Type: " + event.getEventType() + ", Id: " + id);
      jsonRenderer.handleUiEvent(event, res);
    }
    catch (Throwable t) {
      LOG.error("Handling event. Type: " + event.getEventType() + ", Id: " + id, t);
    }
  }

  @Override
  public void valueBound(HttpSessionBindingEvent event) {

  }

  @Override
  public void valueUnbound(HttpSessionBindingEvent event) {
    LOG.info("Terminating json session " + event.getName() + "...");

    //Detach from model
    dispose();

    //Dispose model
    final IClientSession clientSession = getClientSession();
    if (!clientSession.isActive()) {
      //client session was probably already stopped by the model itself
      return;
    }

    new ClientSyncJob("Disposing client session", clientSession) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        clientSession.getDesktop().getUIFacade().fireDesktopClosingFromUI(true);
      }
    }.runNow(new NullProgressMonitor());
    LOG.info("Session " + event.getName() + " terminated.");
  }

}
