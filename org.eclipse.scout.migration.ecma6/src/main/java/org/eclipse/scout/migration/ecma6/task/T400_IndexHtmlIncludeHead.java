package org.eclipse.scout.migration.ecma6.task;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Order(400)
public class T400_IndexHtmlIncludeHead extends AbstractTask{
  private static Path FILE_PATH = Paths.get("src/main/resources/WebContent/index.html");

  @Override
  public boolean accept(Path file, Path moduleRelativeFile, Context context) {
    return file.endsWith(FILE_PATH);
  }

  @Override
  public void process(Path file, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(file);
    String source = workingCopy.getSource();
    String newLineAndIndent = System.lineSeparator()+"    ";

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
        source = MigrationUtility.prependTodo(source, "Could not find '<scout:include template=\"head.html\" />' to replace with '<scout:include template=\"includes/head.html\" />'.");
      }
    }else{
      source = MigrationUtility.prependTodo(source, "Could not find '<scout:include template=\"head.html\" />' to replace with '<scout:include template=\"includes/head.html\" />'.");
    }
    workingCopy.setSource(source);
  }
}
