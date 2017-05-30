# aspectj-ltw-maven-plugin

Maven support plugin for AspectJ load-time weaving. Currently supported:

* aop.xml generation
* TODO: ${project.basedir}/.mvn/jvm.config creation to enable load-time weaving in maven.

## Usage

            <plugin>
                <groupId>com.github.jjYBdx4IL.aop</groupId>
                <artifactId>aspectj-ltw-maven-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate-ltw-config</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

