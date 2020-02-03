import java.nio.file.Path
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.lang.RuntimeException

String groupId = request.groupId
String artifactId = request.artifactId
String packageName = request.package

File outputDirectoryFile = new File(request.outputDirectory)
File projectsDirectoryFile = new File(outputDirectoryFile, artifactId)
Path projectsDirectoryPath = new File(outputDirectoryFile, artifactId).toPath()
File uiModuleFile = projectsDirectoryPath.resolve(artifactId + ".ui").toFile();
File appModuleFile = projectsDirectoryPath.resolve(artifactId + ".app").toFile();

Properties properties = request.properties

String osName = System.properties['os.name'];
boolean windows = osName.toLowerCase().contains('windows');
log('Starting archetype post processing')
log('Using the following request object:')
dumpObject(request)

if (windows) {
  log("Building on windows");
  log("Deleting and renaming files in folder " + appModuleFile);
  deleteFile(new File(appModuleFile, "js build.sh"));
  deleteFile(new File(appModuleFile, "js build.launch"));
  renameFile(new File(appModuleFile, "win js build.cmd"), "js build.cmd");
  renameFile(new File(appModuleFile, "win js build.launch"), "js build.launch");

  log("Deleting and renaming files in folder " + uiModuleFile);
  deleteFile(new File(uiModuleFile, "js tests.sh"));
  deleteFile(new File(uiModuleFile, "js tests.launch"));
  renameFile(new File(uiModuleFile, "win js tests.cmd"), "js tests.cmd");
  renameFile(new File(uiModuleFile, "win js tests.launch"), "js tests.launch");
} else {
  log("Building on " + osName);
  log("Deleting files in folder " + appModuleFile);
  deleteFile(new File(appModuleFile, "win js build.cmd"));
  deleteFile(new File(appModuleFile, "win js build.launch"));

  log("Deleting files in folder " + uiModuleFile);
  deleteFile(new File(uiModuleFile, "win js tests.cmd"));
  deleteFile(new File(uiModuleFile, "win js tests.launch"));

  log("Setting executable permissions");
  new File(appModuleFile, "js build.sh").setExecutable(true, false);
  new File(uiModuleFile, "js tests.sh").setExecutable(true, false);
}

log('Post processing done.')

def deleteFile(File file) {
  if (!file.delete()) {
    throw new RuntimeException("Could not delete file: " + file);
  }
}

def renameFile(File file, String newName) {
  if (!file.renameTo(new File(file.getParent(), newName))) {
    throw new RuntimeException("Could not rename file: " + file);
  }
}

def dumpObject(Object o) {
  String message = o.getClass().getName() + ' [\n'

  o.properties.each{ property, value ->
    message += '  ' + property + ': ' + value + '\n'
  }
  message += ']\n'
  log(message)
}

def log(String message) {
  if (!'true'.equals(request.properties.get('debug'))) {
    return
  }
  SimpleDateFormat format = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
  message = format.format(new Date()) + ' ' + message + '\n'
  new File('archetype-log.txt').append(message)
}
