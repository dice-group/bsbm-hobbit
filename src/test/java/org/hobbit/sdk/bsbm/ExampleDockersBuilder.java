package org.hobbit.sdk.bsbm;

import org.hobbit.sdk.docker.builders.DynamicDockerFileBuilder;

/**
 * @author Pavel Smirnov
 */

public class ExampleDockersBuilder<T> extends DynamicDockerFileBuilder {

  public ExampleDockersBuilder(Class<T> runnerClass, String imageName) throws Exception {
    super("ExampleDockersBuilder");
    imageName(imageName);
    //name for searching in logs
    containerName(runnerClass.getSimpleName());
    //temp docker file will be created there
    buildDirectory(Constants.SDK_BUILD_DIR_PATH);
    //should be packaged will all dependencies (via 'mvn package -DskipTests=true' command)
    jarFilePath(Constants.SDK_JAR_FILE_PATH);
    //will be placed in temp dockerFile
    dockerWorkDir(Constants.SDK_WORK_DIR_PATH);
    //will be placed in temp dockerFile

    addFileOrFolder("filesForDocker.tar.gz");
    //addFileOrFolder("givennames.txt");
    //addFileOrFolder("titlewords.txt");

    runnerClass(org.hobbit.core.run.ComponentStarter.class, runnerClass);
  }

}
