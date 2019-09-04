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
 *
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
      source = processAppListener(appListener,jsFile,source,lineDelimiter);

    }
    wc.setSource(source);
  }

  protected String processAppListener(JsAppListener appListener, JsFile jsFile,String source, String lineDelimiter){

    if (appListener.hasParseErrors()) {
      source = source.replace(appListener.getBody(), appListener.toTodoText(lineDelimiter)+lineDelimiter+ appListener.getBody());
      return source;
    }
    // let instance
    StringBuilder sourceBuilder = new StringBuilder(source);
    if (jsFile.getCopyRight() == null) {
      sourceBuilder.insert(0, "let instance;"+lineDelimiter);
    }
    else {
      sourceBuilder.insert(jsFile.getCopyRight().getEndOffset() + lineDelimiter.length(), "let instance;" + lineDelimiter);
    }

    source  =sourceBuilder.toString();
    // static get
    StringBuilder instanceGetterBuilder = new StringBuilder();
    instanceGetterBuilder.append("static get() {").append(lineDelimiter)
      .append("  return instance;").append(lineDelimiter)
      .append("}").append(lineDelimiter);
    Matcher matcher = Pattern.compile("\\}" + Pattern.quote(T500_CreateClasses.END_CLASS_MARKER)).matcher(source);
    if(matcher.find()){
      source = matcher.replaceAll(instanceGetterBuilder.toString()+"}");
      JsClass jsClass = jsFile.firstJsClass();
      Assertions.assertNotNull(jsClass);
      JsFunction staticGetter = new JsFunction(jsFile.firstJsClass(),"get");
      staticGetter.setStatic(true);
      staticGetter.setBody(instanceGetterBuilder.toString());
      // TODO AHO staticGetter.setAlternaiveReference("scout.device");
      jsClass.addFunction(staticGetter);
    }
    else{
      source = MigrationUtility.prependTodo(source,"Manually create instance static instance getter.",lineDelimiter);
    }


    String body = appListener.getBody();
    String newBody = body.replaceAll(Pattern.quote(appListener.getInstanceNamespace()+"."+appListener.getInstanceName()), "instance");
    source = source.replace(body,newBody);

    return source;
  }
}
