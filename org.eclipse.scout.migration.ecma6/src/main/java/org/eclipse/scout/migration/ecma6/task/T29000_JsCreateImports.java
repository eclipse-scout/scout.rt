package org.eclipse.scout.migration.ecma6.task;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.references.JsImport;
import org.eclipse.scout.rt.platform.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(29000)
public class T29000_JsCreateImports extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(T29000_JsCreateImports.class);

  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"));

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    JsFile jsFile = context.ensureJsFile(workingCopy);
    String lineDelimiter = workingCopy.getLineSeparator();
    // create imports
    Collection<JsImport> imports = jsFile.getImports();
    if (imports.isEmpty()) {
      return;
    }
    String importsSource = imports.stream()
        .map(imp -> imp.toSource(context))
        .collect(Collectors.joining(lineDelimiter));

    StringBuilder sourceBuilder = new StringBuilder(workingCopy.getSource());
    if (jsFile.getCopyRight() == null) {
      sourceBuilder.insert(0, importsSource + lineDelimiter);
    }
    else {

      Matcher matcher = Pattern.compile(Pattern.quote(jsFile.getCopyRight().getSource())).matcher(sourceBuilder.toString());
      if(matcher.find()){
        sourceBuilder.insert(matcher.end(), importsSource+ lineDelimiter);
      }else{
        sourceBuilder.insert(0, MigrationUtility.prependTodo("","insert 'var instance;' manual.",lineDelimiter));
        LOG.warn("Could not find end of copyright in file '"+jsFile.getPath()+"'");
      }
    }
    workingCopy.setSource(sourceBuilder.toString());

  }
}
