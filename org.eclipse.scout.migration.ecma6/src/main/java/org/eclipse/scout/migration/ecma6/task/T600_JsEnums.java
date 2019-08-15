package org.eclipse.scout.migration.ecma6.task;

import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsEnum;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class T600_JsEnums extends AbstractTask{
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"), PathFilters.isClass());

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    String source = workingCopy.getSource();
    JsFile jsFile = context.ensureJsFile(workingCopy);

    for(JsClass clazz : jsFile.getJsClasses()){
      source = updateEnums(source, clazz,  workingCopy.getLineSeparator());
    }

    workingCopy.setSource(source);
  }

  protected String updateEnums(String source, JsClass clazz, String lineDelimiter){
    List<JsEnum> jsEnums = clazz.getEnums();
    if(jsEnums.size() == 0){
      return source;
    }
    for(JsEnum jsEnum : jsEnums){
      source = updateEnum(source, jsEnum,  lineDelimiter);
    }
    return  source;
  }

  protected String updateEnum(String source, JsEnum jsEnum,String lineDelimiter){
    StringBuilder patternBuilder = new StringBuilder();
    patternBuilder.append(jsEnum.getJsClass().getNamespace())
      .append("\\.")
      .append(jsEnum.getJsClass().getName());

    patternBuilder.append("\\.").append(jsEnum.getName());
    patternBuilder.append("(\\ \\=\\s*\\{)");

    Pattern pattern = Pattern.compile(patternBuilder.toString());
    Matcher matcher = pattern.matcher(source);
    if (matcher.find()) {

      StringBuilder replacement = new StringBuilder();
      if(jsEnum.hasParseErrors()){
        replacement.append(jsEnum.toTodoText(lineDelimiter)).append(lineDelimiter);
        replacement.append(matcher.group());
      }else{
        replacement.append("export const ").append(jsEnum.getName())
          .append(matcher.group(1));
      }
      source = matcher.replaceFirst(replacement.toString());
    }
    return source;
  }
}
