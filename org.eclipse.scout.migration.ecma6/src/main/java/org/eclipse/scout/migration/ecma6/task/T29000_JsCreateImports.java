package org.eclipse.scout.migration.ecma6.task;

import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsImport;
import org.eclipse.scout.rt.platform.Order;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Order(2900)
public class T29000_JsCreateImports extends AbstractTask{
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"));

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    JsFile jsFile = context.ensureJsFile(workingCopy);
    // create imports
    Collection<JsImport> imports = jsFile.getImports();
    if(imports.isEmpty()){
      return;
    }
    String impots  = imports.stream()
      .map(imp -> imp.toSource(context))
      .collect(Collectors.joining(workingCopy.getLineSeparator()));

    StringBuilder sourceBuilder = new StringBuilder(workingCopy.getSource());
    if (jsFile.getCopyRight() == null) {
      sourceBuilder.insert(0, impots+workingCopy.getLineSeparator());
    }
    else {
      sourceBuilder.insert(jsFile.getCopyRight().getEndOffset() + workingCopy.getLineSeparator().length(), impots+workingCopy.getLineSeparator());
    }
    workingCopy.setSource(sourceBuilder.toString());

  }
}
