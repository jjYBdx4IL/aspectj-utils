# aspectj-ltw-maven-plugin

Maven support plugin for AspectJ load-time weaving. Currently supported:

* aop.xml generation
* ${project.basedir}/.mvn/jvm.config creation to enable load-time weaving in maven.

## Usage

            <plugin>
                <groupId>com.github.jjYBdx4IL.aop</groupId>
                <artifactId>aspectj-ltw-maven-plugin</artifactId>
                <version>@project.version@</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate-ltw-config</goal>
                            <goal>generate-maven-startup-config</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <aspectjWeaverVersion>1.8.7</aspectjWeaverVersion>
                </configuration>
            </plugin>

