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

import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class DeletingTemplateFileVisitor implements FileVisitor<Path> {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveUnnecessaryFilesTask)

    private static final List<String> DIR_PATTERN = ['lib', '.settings', '.idea']

    private static final List<String> FILE_PATTERN = ['.classpath', '.project', 'GSCollectionsKata.iml']

    private final Project project

    DeletingTemplateFileVisitor(Project project) {
        this.project = project
    }

    @Override
    FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        def skip = DIR_PATTERN.findAll{dir.endsWith(it)}.size() > 0
        if (skip) {
            LOG.debug ("skip and delete directory [${dir}]")
            project.delete dir.toFile()
        }
        return skip? FileVisitResult.SKIP_SUBTREE: FileVisitResult.CONTINUE;
    }

    @Override
    FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        def del = FILE_PATTERN.findAll{file.endsWith(it)}.size() > 0
        if(del) {
            LOG.debug ("delete file [${file}]")
            project.delete file.toFile()
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        if (exc != null) {
            LOG.debug("error on file: [${file}]", exc)
            throw exc
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc != null) {
            LOG.debug("error on directory: [${dir}]", exc)
            throw exc
        }
        return FileVisitResult.CONTINUE;
    }
}
