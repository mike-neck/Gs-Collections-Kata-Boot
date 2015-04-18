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

class DownloadZip extends DefaultTask {

    private static final String KATA_URL = 'https://github.com/goldmansachs/gs-collections-kata/archive/master.zip'

    @OutputFile
    File destination

    @TaskAction
    void download() {
        def ant = getProject().ant
        ant.get (src: KATA_URL, dest: destination)
    }

    DownloadZip downloadTo(File destination) {
        this.destination = destination
        return this
    }

    DownloadZip downloadTo(String destination) {
        this.downloadTo(getProject().file(destination))
    }
}
