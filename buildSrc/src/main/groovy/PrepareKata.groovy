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
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static KataFiles.*

class PrepareKata extends DefaultTask {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareKata)

    private String templateDirName = 'template'

    private String unArchivedDirName = 'kata'

    @OutputFiles
    private File company

    @OutputFiles
    private File customer

    @OutputFiles
    private File lineItem

    @OutputFiles
    private File order

    @OutputFiles
    private File supplier

    @OutputFiles
    private File domain

    PrepareKata() {
        super()
        def root = project.projectDir.toPath()
        def srcDir = project.sourceSets.main.java.srcDirs.find{it != null}.toPath()
        def testDir = project.sourceSets.test.java.srcDirs.find{it != null}.toPath()
        def srcMainJava = root.relativize(srcDir).toString()
        def srcTestJava = root.relativize(testDir).toString()
        company = project.file("${srcMainJava}${PACKAGE}${COMPANY}")
        customer = project.file("${srcMainJava}${PACKAGE}${CUSTOMER}")
        lineItem = project.file("${srcMainJava}${PACKAGE}${LINE_ITEM}")
        order = project.file("${srcMainJava}${PACKAGE}${ORDER}")
        supplier = project.file("${srcMainJava}${PACKAGE}${SUPPLIER}")
        domain = project.file("${srcTestJava}${PACKAGE}${DOMAIN}")
        outputs.files(company, customer, lineItem, order, supplier, domain)
        LOG.info("PrepareKata is initialized with project[${project}]")
    }

    @TaskAction
    void copy() {
        def template = templateDirName
        def unpacked = unArchivedDirName
        [
                [from: COMPANY, to: company, srcScope: true],
                [from: CUSTOMER, to: customer, srcScope: true],
                [from: LINE_ITEM, to: lineItem, srcScope: true],
                [from: ORDER, to: order, srcScope: true],
                [from: SUPPLIER, to: supplier, srcScope: true],
                [from: DOMAIN, to: domain, srcScope: false]
        ].each {
            def file = project.file("${template}/${unpacked}/${MASTER}/${it.srcScope? SRC_MAIN_JAVA:SRC_TEST_JAVA}/${PACKAGE}/${it.from}")
            def list = []
            file.text.eachLine {String line ->
                if (!it.srcScope || !line.contains('import') || !line.contains('Assert')) {
                    list << line.replace('Assert.fail', 'if(1 == 1) throw new UnsupportedOperationException')
                }
            }
            it.to.write(list.join('\n'))
        }
    }

    public PrepareKata root(String dirName) {
        this.templateDirName = dirName
        return this
    }

    public PrepareKata unpacked(String dirName) {
        this.unArchivedDirName = dirName
        return this
    }
}
