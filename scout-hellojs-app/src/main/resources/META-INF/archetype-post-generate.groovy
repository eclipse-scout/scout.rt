import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.SecureRandom
import java.text.SimpleDateFormat

//noinspection GrUnresolvedAccess
String artifactId = request.artifactId
//noinspection GrUnresolvedAccess
File outputDirectoryFile = new File(request.outputDirectory as String)
Path projectsDirectoryPath = new File(outputDirectoryFile, artifactId).toPath()
File uiModuleFile = projectsDirectoryPath.resolve(artifactId.concat('.ui')).toFile()
File appModuleFile = projectsDirectoryPath.resolve(artifactId.concat('.app')).toFile()
File appDevModuleFile = projectsDirectoryPath.resolve(artifactId.concat('.app.dev')).toFile()

String osName = System.properties.get('os.name')
log('Starting archetype post processing')
log('Using the following request object:')
//noinspection GrUnresolvedAccess
dumpObject(request)

log('Building on '.concat(osName))
if (osName.toLowerCase().contains('windows')) {
    log('Deleting and renaming files in folder '.concat(appModuleFile.toString()))
    deleteFile(new File(appModuleFile, 'js build.sh'))
    deleteFile(new File(appModuleFile, 'js build.launch'))
    renameFile(new File(appModuleFile, 'win js build.cmd'), 'js build.cmd')
    renameFile(new File(appModuleFile, 'win js build.launch'), 'js build.launch')

    log('Deleting and renaming files in folder '.concat(uiModuleFile.toString()))
    deleteFile(new File(uiModuleFile, 'js tests.sh'))
    deleteFile(new File(uiModuleFile, 'js tests.launch'))
    renameFile(new File(uiModuleFile, 'win js tests.cmd'), 'js tests.cmd')
    renameFile(new File(uiModuleFile, 'win js tests.launch'), 'js tests.launch')
} else {
    log('Deleting files in folder '.concat(appModuleFile.toString()))
    deleteFile(new File(appModuleFile, 'win js build.cmd'))
    deleteFile(new File(appModuleFile, 'win js build.launch'))

    log('Deleting files in folder '.concat(uiModuleFile.toString()))
    deleteFile(new File(uiModuleFile, 'win js tests.cmd'))
    deleteFile(new File(uiModuleFile, 'win js tests.launch'))

    log('Setting executable permissions')
    new File(appModuleFile, 'js build.sh').setExecutable(true, false)
    new File(uiModuleFile, 'js tests.sh').setExecutable(true, false)
}

writeDbPassword(appDevModuleFile)

log('Post processing done.')

static def deleteFile(File file) {
    if (!file.delete()) {
        throw new RuntimeException('Could not delete file: '.concat(file.toString()))
    }
}

static def renameFile(File file, String newName) {
    if (!file.renameTo(new File(file.getParent(), newName))) {
        throw new RuntimeException('Could not rename file: '.concat(file.toString()))
    }
}

static def writeDbPassword(File appDevModuleFile) {
    Path dbConfig = new File(appDevModuleFile, 'src/main/resources/dev.db.config.properties').toPath()
    byte[] rnd = new byte[32]
    new SecureRandom().nextBytes(rnd)
    String pwd = new BigInteger(1, rnd).toString(32)
    String content = Files.readString(dbConfig, StandardCharsets.UTF_8).replace('changeme', pwd)
    Files.writeString(dbConfig, content, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
}

def dumpObject(Object o) {
    String message = o.getClass().getName() + ' [\n'

    o.properties.each { property, value ->
        message += '  ' + property + ': ' + value + '\n'
    }
    message += ']\n'
    log(message)
}

def log(String message) {
    //noinspection GrUnresolvedAccess
    if (request.properties.get('debug') != 'true') {
        return
    }
    SimpleDateFormat format = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
    message = format.format(new Date()) + ' ' + message + '\n'
    new File('archetype-log.txt').append(message as Object, StandardCharsets.UTF_8.name(), false)
}
