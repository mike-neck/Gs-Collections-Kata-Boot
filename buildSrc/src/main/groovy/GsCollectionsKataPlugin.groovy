/*
 * Copyright 2015 Shinya Mochida
 *
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import static KataFiles.*

class GsCollectionsKataPlugin implements Plugin<Project> {

    private static final Logger LOG = LoggerFactory.getLogger(GsCollectionsKataPlugin)

    private static final String KATA = 'Gs-Collections-Kata'

    private String tempDirName = 'template'

    private String destinationFileName = 'kata.zip'

    private String unpacked = 'kata'

    private boolean useGit = true

    private Project project

    private Delete deleteTemplate

    private DownloadZip downloadKata

    private Copy unArchiveKata

    private RemoveUnnecessaryFilesTask removeUnnecessaryFile

    private PrepareKata prepareKata

    private Copy exercise

    private ResetKata resetKata

    @Override
    void apply(Project project) {
        this.project = project
        this.deleteTemplate = defineCleanZip()
        this.downloadKata = defineDownloadKata()
        this.unArchiveKata = defineUnArchiveKata()
        this.removeUnnecessaryFile = definePrepareTemplate()
        this.prepareKata = definePrepare()
        this.exercise = defineExercise()
        this.resetKata = defineResetKata()
    }

    private Delete defineCleanZip() {
        Delete task = project.task(type: Delete,'deleteTemplate') as Delete
        LOG.debug("defining ${task.name} task")
        task.group = KATA
        task.description = 'delete all files from template directory'
        task.delete project.file(tempDirName)
    }

    private DownloadZip defineDownloadKata() {
        def task = project.task(type: DownloadZip,'downloadKata') as DownloadZip
        LOG.debug("defining ${task.name} task")
        task.dependsOn deleteTemplate
        task.description = 'downloads zip file from gs-collections-kata project into template directory(depends on deleteTemplate)'
        task.downloadTo "${tempDirName}/${destinationFileName}"
    }

    private Copy defineUnArchiveKata() {
        def task = project.task(type: Copy, 'unArchiveKata') as Copy
        LOG.debug("defining ${task.name} task")
        task.dependsOn downloadKata
        task.description = 'unarchive the zip file from gs-collections-kata project(depends on downloadKata)'
        task.from project.zipTree(downloadKata.destination)
        task.into project.file("${tempDirName}/${unpacked}")
    }

    private RemoveUnnecessaryFilesTask definePrepareTemplate() {
        def task = project.task(type: RemoveUnnecessaryFilesTask, dependsOn: unArchiveKata, 'removeUnnecessaryFile') as RemoveUnnecessaryFilesTask
        LOG.debug("defining ${task.name} task")
        task.description = 'remove unnecessary files(like jar, because it is available via gradle)'
        task.useGit useGit
        task.templateDir tempDirName
    }

    private PrepareKata definePrepare() {
        def task = project.task(type: PrepareKata, dependsOn: removeUnnecessaryFile, 'prepareKata') as PrepareKata
        LOG.debug("defining ${task.name} task")
        task.group = KATA
        task.description = 'prepare template kata project'
        task.root tempDirName
        task.unpacked unpacked
    }

    private Copy defineExercise() {
        LOG.debug("defining exercise task which is be done next")
        def set = prepareKata.outputs.files.files
        def copied = set.findAll{it.exists()}
        if (set.size() != copied.size()) {
            LOG.debug("prepareKata is not finished yet.")
            return null
        }

        def allFiles = packageContents(Paths.get(tempDirName, unpacked, MASTER, SRC_TEST_JAVA)).collect {it.fileName}
        def current = packageContents(srcTestJava()).collect {it.fileName}

        def diff = allFiles - current
        if (diff.size() == 0) {
            LOG.debug('all exercise is finished or kata is not downloaded.')
            return null
        }
        def javaFilePath = diff.sort{it.toString()}.find{it.toString().startsWith('Exercise')}
        if (javaFilePath == null || javaFilePath.toString().isEmpty()) {
            def msg = "Illegal state with files.(all[${allFiles.join(', ')}], current[${current.join(', ')}])"
            LOG.debug(msg)
            throw new IllegalStateException(msg)
        }

        def javaFile = javaFilePath.toString()
        def taskName = javaFile.toLowerCase().replace('test.java', '')

        def task = project.task(type: Copy, taskName) as Copy
        task.group = KATA
        task.description = "try ${taskName}[$javaFile]"
        task.from project.file("${tempDirName}/${unpacked}/${MASTER}/${SRC_TEST_JAVA}/${PACKAGE}/${javaFile}")
        task.into resolvePackage(srcTestJava()).toFile()
    }

    private ResetKata defineResetKata() {
        List<Path> list = [packageContents(srcMainJava()), packageContents(srcTestJava())].flatten().findAll{Path path ->
            !path.endsWith('.gitkeep')
        }
        LOG.debug("reset object [${list.join(', ')}]")
        if (list.size == 0) {
            LOG.debug('There are no files')
            return null
        }
        def task = project.task(type: ResetKata, 'resetKata') as ResetKata
        task.group = KATA
        task.description = 'remove all training files'
        task.remove list
    }

    private Path srcMainJava() {
        retrieveFromSourceSets(project.sourceSets.main)
    }

    private Path srcTestJava() {
        retrieveFromSourceSets(project.sourceSets.test)
    }

    static Path retrieveFromSourceSets(SourceSet set) {
        set.java.srcDirs.find{it != null}.toPath()
    }

    static List<Path> packageContents(Path path) {
        fetchChildren(resolvePackage(path))
    }

    static Path resolvePackage(Path path) {
        path.resolve(PACKAGE)
    }

    static List<Path> fetchChildren(Path path) {
        Files.list(path).collect(Collectors.toList())
    }

    public GsCollectionsKataPlugin useGit(boolean git) {
        this.useGit = git
        return this
    }
}
