/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
Path parentModulePath = projectsDirectoryPath.resolve(artifactId)
File uiModuleFile = projectsDirectoryPath.resolve(artifactId.concat('.ui')).toFile()
File appModuleFile = projectsDirectoryPath.resolve(artifactId.concat('.app')).toFile()
File runConfigsFile = parentModulePath.resolve('run-configs').toFile()
File appDevModuleFile = projectsDirectoryPath.resolve(artifactId.concat('.app.dev')).toFile()

String osName = System.properties.get('os.name')
log('Starting archetype post processing')
log('Using the following request object:')
//noinspection GrUnresolvedAccess
dumpObject(request)

log('Building on '.concat(osName))
if (osName.toLowerCase().contains('windows')) {
    log('Deleting and renaming files in folder '.concat(runConfigsFile.toString()))
    deleteFile(new File(runConfigsFile, 'js build.sh'))
    deleteFile(new File(runConfigsFile, 'js build.launch'))
    renameFile(new File(runConfigsFile, 'win js build.cmd'), 'js build.cmd')
    renameFile(new File(runConfigsFile, 'win js build.launch'), 'js build.launch')
    deleteFile(new File(runConfigsFile, 'js tests.sh'))
    deleteFile(new File(runConfigsFile, 'js tests.launch'))
    renameFile(new File(runConfigsFile, 'win js tests.cmd'), 'js tests.cmd')
    renameFile(new File(runConfigsFile, 'win js tests.launch'), 'js tests.launch')
} else {
    log('Deleting files in folder '.concat(runConfigsFile.toString()))
    deleteFile(new File(runConfigsFile, 'win js build.cmd'))
    deleteFile(new File(runConfigsFile, 'win js build.launch'))
    deleteFile(new File(runConfigsFile, 'win js tests.cmd'))
    deleteFile(new File(runConfigsFile, 'win js tests.launch'))

    log('Setting executable permissions')
    new File(runConfigsFile, 'js build.sh').setExecutable(true, false)
    new File(runConfigsFile, 'js tests.sh').setExecutable(true, false)
}

log('Rename IntelliJ run configs.')
renameFile(new File(runConfigsFile, 'Generate jooq classes based on DB.run_.xml'), 'Generate jooq classes based on DB.run.xml')
renameFile(new File(runConfigsFile, 'js build.run_.xml'), 'js build.run.xml')
renameFile(new File(runConfigsFile, 'js tests.run_.xml'), 'js tests.run.xml')
renameFile(new File(runConfigsFile, 'Launch dev server.run_.xml'), 'Launch '.concat(artifactId).concat('dev server.run.xml'))
renameFile(new File(runConfigsFile, 'launch all.run_.xml'), 'launch all.run.xml')
renameFile(new File(runConfigsFile, 'pnpm-install.run_.xml'), 'pnpm-install.run.xml')
renameFile(new File(runConfigsFile, 'Setup local dev database.run_.xml'), 'Setup local dev database.run.xml')
renameFile(new File(uiModuleFile,  'dotgitignore'), '.gitignore')
renameFile(new File(appModuleFile,  'dotgitignore'), '.gitignore')

if (request.properties.get('skipChangeMeReplacement') != 'true') {
  writeDbPassword(appDevModuleFile)
}

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

def writeDbPassword(File appDevModuleFile) {
    log('Generate random password for dev db')
    Path dbConfig = new File(appDevModuleFile, 'src/main/resources/dev.db.config.properties').toPath()
    byte[] rnd = new byte[32]
    new SecureRandom().nextBytes(rnd)
    String pwd = new BigInteger(1, rnd).toString(32)

    log('replace password in dev.db.config.properties')
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
    new File('archetype-log.txt').append(message)
}
