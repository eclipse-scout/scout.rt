/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6.task;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsAppListener;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsFunction;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FROM:
 * 
 * <pre>
 *   scout.addAppListener('prepare', function() {
 *   if (scout.device) {
 *     // if the device was created before the app itself, use it instead of creating a new one
 *     return;
 *   }
 *   scout.device = scout.create('Device', {
 *     userAgent: navigator.userAgent
 *   });
 * });
 * </pre>
 *
 * TO:
 *
 * <pre>
 *   let instance;
 *   App.addAppListener('prepare', function() {
 *   if (scout.device) {
 *     // if the device was created before the app itself, use it instead of creating a new one
 *     return;
 *   }
 *   instance = scout.create('Device', {
 *     userAgent: navigator.userAgent
 *   });
 * });
 * </pre>
 */
@Order(710)
public class T710_Singletons extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(T710_Singletons.class);
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"), PathFilters.isClass());

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy wc = context.ensureWorkingCopy(pathInfo.getPath());
    JsFile jsFile = context.ensureJsFile(wc);

    String source = wc.getSource();
    String lineDelimiter = wc.getLineSeparator();
    for (JsAppListener appListener : jsFile.getAppListeners()) {
      source = processAppListener(appListener, jsFile, source, lineDelimiter, context);

    }
    wc.setSource(source);
  }

  protected String processAppListener(JsAppListener appListener, JsFile jsFile, String source, String lineDelimiter, Context context) {

    if (appListener.hasParseErrors()) {
      source = source.replace(appListener.getSource(), appListener.toTodoText(lineDelimiter) + lineDelimiter + appListener.getSource());
      return source;
    }
    // let instance
    StringBuilder sourceBuilder = new StringBuilder(source);
    if (jsFile.getCopyRight() == null) {
      sourceBuilder.insert(0, "let instance;" + lineDelimiter);
    }
    else {
      Matcher matcher = Pattern.compile(Pattern.quote(jsFile.getCopyRight().getSource())).matcher(sourceBuilder.toString());
      if(matcher.find()){
        sourceBuilder.insert(matcher.end(), "let instance;" + lineDelimiter);
      }else{
        LOG.warn("Could not find end of copyright in file '"+jsFile.getPath()+"'");
        sourceBuilder.insert(0,MigrationUtility.prependTodo("","insert 'var instance;' manual.",lineDelimiter));
      }
    }

    source = sourceBuilder.toString();

    source = createInstanceGetter(jsFile,source,lineDelimiter);


    String body = appListener.getSource();
    String newBody = body.replaceAll(Pattern.quote(appListener.getInstanceNamespace() + "." + appListener.getInstanceName()), "instance");
    source = source.replace(body, newBody);

    return source;
  }

  protected String createInstanceGetter(JsFile jsFile, String source, String lineDelimiter){
    // static get
    JsClass jsClass = jsFile.firstJsClass();
    if(jsClass == null){
      source = MigrationUtility.prependTodo(source, "Manually create instance static instance getter.", lineDelimiter);
      LOG.warn("Could not create static instance getter in '"+jsFile.getPath()+"'");
      return source;
    }
    JsFunction instanceGetter = jsClass.getFunctions().stream()
      .filter(fun -> fun.isStatic())
      .filter(fun -> "get".equals(fun.getName()))
      .findFirst().orElse(null);
    if(instanceGetter == null){
      source = MigrationUtility.prependTodo(source, "Manually create instance static instance getter.", lineDelimiter);
      return source;
    }


    Matcher matcher = Pattern.compile("\\}" + Pattern.quote(T500_CreateClasses.END_CLASS_MARKER)).matcher(source);
    if (matcher.find()) {
      source = matcher.replaceAll(lineDelimiter+instanceGetter.getSource() + "}");
    }
    else {
      source = MigrationUtility.prependTodo(source, "Manually create instance static instance getter.", lineDelimiter);
      LOG.warn("Could not create static instance getter in '"+jsFile.getPath()+"'");
    }
    return source;
  }
}
