import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.GeneralSecurityException
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.*
import java.text.SimpleDateFormat

//noinspection GrUnresolvedAccess
String artifactId = request.artifactId
//noinspection GrUnresolvedAccess
File outputDirectoryFile = new File(request.outputDirectory as String)
Path projectsDirectoryPath = new File(outputDirectoryFile, artifactId).toPath()
File uiModuleFile = projectsDirectoryPath.resolve(artifactId.concat('.ui.html')).toFile()

File serverAppDevModuleFile = projectsDirectoryPath.resolve(artifactId.concat('.server.app.dev')).toFile()
File uiAppDevModuleFile = projectsDirectoryPath.resolve(artifactId.concat('.ui.html.app.dev')).toFile()

File serverAppWarModuleFile = projectsDirectoryPath.resolve(artifactId.concat('.server.app.war')).toFile()
File uiAppWarModuleFile = projectsDirectoryPath.resolve(artifactId.concat('.ui.html.app.war')).toFile()

String osName = System.properties.get('os.name')
log('Starting archetype post processing')
log('Using the following request object:')
//noinspection GrUnresolvedAccess
dumpObject(request)

log('Building on '.concat(osName))
if (osName.toLowerCase().contains('windows')) {
    log('Deleting and renaming files in folder '.concat(uiModuleFile.toString()))
    deleteFile(new File(uiModuleFile, 'js build.sh'))
    deleteFile(new File(uiModuleFile, 'js build.launch'))

    renameFile(new File(uiModuleFile, 'win js build.cmd'), 'js build.cmd')
    renameFile(new File(uiModuleFile, 'win js build.launch'), 'js build.launch')
} else {
    log('Deleting files in folder '.concat(uiModuleFile.toString()))
    deleteFile(new File(uiModuleFile, 'win js build.cmd'))
    deleteFile(new File(uiModuleFile, 'win js build.launch'))

    log('Setting executable permissions')
    new File(uiModuleFile, 'js build.sh').setExecutable(true, false)
}

writeKeyPair(serverAppWarModuleFile, uiAppWarModuleFile)
writeKeyPair(serverAppDevModuleFile, uiAppDevModuleFile)

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

static def writeKeyPair(File serverModuleDir, File uiModuleDir) {
    String[] keyPair = generateKeyPairSafe()
    String configPropertiesPath = 'src/main/resources/config.properties'

    Path uiConfig = new File(uiModuleDir, configPropertiesPath).toPath()
    replaceIn(uiConfig, 'changeme', keyPair[0])

    Path serverConfig = new File(serverModuleDir, configPropertiesPath).toPath()
    replaceIn(serverConfig, 'changeme', keyPair[1])
}

static def replaceIn(Path file, String search, String replace) {
    String content = Files.readString(file, StandardCharsets.UTF_8).replace(search, replace)
    Files.writeString(file, content, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
}

static def generateKeyPairSafe() {
    try {
        return generateKeyPair()
    }
    catch (GeneralSecurityException e) {
        log(e.message)
        String keyPlaceholder = "TODO_use_org.eclipse.scout.rt.platform.security.SecurityUtility.main(String[])"
        String[] fallback = new String[2]
        fallback[0] = keyPlaceholder
        fallback[1] = keyPlaceholder
        return fallback
    }
}

/**
 * Creates a new key pair (private and public key) compatible with the Scout Runtime.<br>
 * <b>This method must behave exactly like the one implemented in
 * org.eclipse.scout.rt.platform.security.SecurityUtility#generateKeyPair().</b>
 *
 * @return A string array of length=2 containing the base64 encoded private key at index zero and the base64 encoded public key at index 1.
 */
static def generateKeyPair() {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "SunEC")
    AlgorithmParameterSpec spec = new ECGenParameterSpec("secp256k1")
    keyGen.initialize(spec, new SecureRandom())
    KeyPair keyPair = keyGen.generateKeyPair()

    EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded())
    EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded())

    Base64.Encoder base64Encoder = Base64.getEncoder()

    String[] result = new String[2]
    result[0] = base64Encoder.encodeToString(pkcs8EncodedKeySpec.getEncoded()) // private key
    result[1] = base64Encoder.encodeToString(x509EncodedKeySpec.getEncoded()) // public key
    return result
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
