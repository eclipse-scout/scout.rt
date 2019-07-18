package org.eclipse.scout.migration.ecma6.task;

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.rt.platform.Order;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Order(400)
public class T400_IndexHtmlIncludeHead extends AbstractTask{
  private Predicate<PathInfo> m_pathFilter = PathFilters.oneOf(Paths.get("src/main/resources/WebContent/index.html"));

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_pathFilter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    String source = workingCopy.getSource();

    Document doc = Jsoup.parse(source);
    Elements scoutStylesheetElements = doc.getElementsByTag("scout:include");
    Element headIncludeTag = scoutStylesheetElements.stream()
      .filter(e -> {
        String template = e.attr("template");
        return template != null && template.equalsIgnoreCase("head.html");
      })
    .findFirst().orElse(null);
    if(headIncludeTag != null){

      Pattern pattern = Pattern.compile("(\\s*)"+Pattern.quote(headIncludeTag.outerHtml()));
      Matcher matcher = pattern.matcher(source);
      if(matcher.find()){
        source = matcher.replaceAll(matcher.group(1)+"<scout:include template=\"includes/head.html\" />");
      }else{
        source = MigrationUtility.prependTodo(source, "Could not find '<scout:include template=\"head.html\" />' to replace with '<scout:include template=\"includes/head.html\" />'.",workingCopy.getLineSeparator());
      }
    }else{
      source = MigrationUtility.prependTodo(source, "Could not find '<scout:include template=\"head.html\" />' to replace with '<scout:include template=\"includes/head.html\" />'.",workingCopy.getLineSeparator());
    }
    workingCopy.setSource(source);
  }
}
