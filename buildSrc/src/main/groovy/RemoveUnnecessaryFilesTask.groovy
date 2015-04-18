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
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RemoveUnnecessaryFilesTask extends DefaultTask {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveUnnecessaryFilesTask)

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private boolean removeGit = false

    private String file = 'removed.txt'

    private String rootDirName = 'template'

    private String zone = 'UTC'

    @OutputFile
    private File finished

    @TaskAction
    protected void clean() {
        project.delete(finished)
        def root = Paths.get(rootDirName)
        def visitor = new DeletingTemplateFileVisitor(getProject())
        Files.walkFileTree(root, visitor)
        def z = ZoneId.of(zone)
        if (removeGit && project.file('.git').exists()) {
            LOG.info("useGit is set ${!removeGit}, .git is detected.")
            project.delete(project.file('.git'))
        }
        finished.write(LocalDateTime.now(z).format(FORMATTER))
    }

    public RemoveUnnecessaryFilesTask templateDir(String rootDirName) {
        this.rootDirName = rootDirName
        finished = project.file("${rootDirName}/${file}")
        return this
    }

    public RemoveUnnecessaryFilesTask timezone(String zone) {
        this.zone = zone
        return this
    }

    public RemoveUnnecessaryFilesTask useGit(boolean git) {
        this.removeGit = !git
        return this
    }
}
