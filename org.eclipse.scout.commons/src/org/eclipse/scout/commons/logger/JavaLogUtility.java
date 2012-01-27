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
package org.eclipse.scout.commons.logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JavaLogUtility {
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([0-9]*)\\}", Pattern.DOTALL);

  private JavaLogUtility() {
  }

  private static final HashMap<Level, Integer> levelMap;

  static {
    levelMap = new HashMap<Level, Integer>();
    levelMap.put(Level.OFF, IScoutLogger.LEVEL_OFF);
    levelMap.put(Level.SEVERE, IScoutLogger.LEVEL_ERROR);
    levelMap.put(Level.WARNING, IScoutLogger.LEVEL_WARN);
    levelMap.put(Level.INFO, IScoutLogger.LEVEL_INFO);
    levelMap.put(Level.CONFIG, IScoutLogger.LEVEL_INFO);
    levelMap.put(Level.FINE, IScoutLogger.LEVEL_DEBUG);
    levelMap.put(Level.FINER, IScoutLogger.LEVEL_DEBUG);
    levelMap.put(Level.FINEST, IScoutLogger.LEVEL_TRACE);
    levelMap.put(Level.ALL, IScoutLogger.LEVEL_TRACE);
  }

  /**
   * convert between java log {@link Level} (object-based) and scout level (int-based)
   */
  public static int javaToScoutLevel(Level level) {
    Integer l = levelMap.get(level);
    if (l == null) {
      l = IScoutLogger.LEVEL_WARN;
    }
    return l;
  }

  /**
   * convert between java log {@link Level} (object-based) and scout level (int-based)
   */
  public static Level scoutToJavaLevel(int level) {
    switch (level) {
      case IScoutLogger.LEVEL_OFF:
        return Level.OFF;
      case IScoutLogger.LEVEL_ERROR:
        return Level.SEVERE;
      case IScoutLogger.LEVEL_WARN:
        return Level.WARNING;
      case IScoutLogger.LEVEL_INFO:
        return Level.INFO;
      case IScoutLogger.LEVEL_DEBUG:
        return Level.FINE;
      case IScoutLogger.LEVEL_TRACE:
        return Level.FINEST;
      default:
        return Level.WARNING;
    }
  }

  public static LogRecord buildLogRecord(Class wrapperClass, Level level, String format, Object[] args) {
    //replace args
    String msg;
    if (format != null && args != null && args.length > 0) {
      Matcher m = VARIABLE_PATTERN.matcher(format);
      StringBuffer buf = new StringBuffer();
      int endPos = 0;
      int index = 0;
      while (m.find() && index < args.length) {
        buf.append(format.substring(endPos, m.start()));
        buf.append(args[index]);
        endPos = m.end();
        index++;
      }
      if (endPos < format.length()) {
        buf.append(format.substring(endPos));
      }
      msg = buf.toString();
    }
    else {
      msg = format;
    }
    //
    LogRecord record = new LogRecord(level, msg);
    //
    //class, method, line
    StackTraceElement e = getCallerLine(wrapperClass);
    if (e != null) {
      //class.method: org.eclipse.foo.bar.MyClass.myMethod(MyClass.java:123)
      record.setSourceClassName(e.getClassName() + "." + e.getMethodName() + "(" + e.getFileName() + ":" + e.getLineNumber() + ")");
      record.setSourceMethodName(null);
    }
    return record;
  }

  private static StackTraceElement getCallerLine(Class wrapperClass) {
    try {
      StackTraceElement[] trace = new Exception().getStackTrace();
      int traceIndex = 0;
      HashSet<String> ignoredPackagePrefixes = new HashSet<String>();
      ignoredPackagePrefixes.add(IScoutLogger.class.getPackage().getName());
      if (wrapperClass != null) {
        ignoredPackagePrefixes.add(wrapperClass.getPackage().getName());
      }
      while (traceIndex < trace.length) {
        boolean found = true;
        for (String prefix : ignoredPackagePrefixes) {
          if (trace[traceIndex].getClassName().startsWith(prefix)) {
            found = false;
            break;
          }
        }
        if (found) {
          break;
        }
        traceIndex++;
      }
      if (traceIndex >= trace.length) {
        traceIndex = trace.length - 1;
      }
      return trace[traceIndex];
    }
    catch (Throwable t) {
      return null;
    }
  }

}
