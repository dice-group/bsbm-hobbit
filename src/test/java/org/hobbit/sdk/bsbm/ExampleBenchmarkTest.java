package org.hobbit.sdk.bsbm;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.ComponentsExecutor;
import org.hobbit.sdk.EnvironmentVariablesWrapper;
import org.hobbit.sdk.JenaKeyValue;
import org.hobbit.sdk.bsbm.benchmark.BenchmarkController;
import org.hobbit.sdk.bsbm.benchmark.DataGenerator;
import org.hobbit.sdk.bsbm.benchmark.EvalModule;
import org.hobbit.sdk.bsbm.benchmark.EvalStorage;
import org.hobbit.sdk.bsbm.benchmark.TaskGenerator;
import org.hobbit.sdk.bsbm.helper.ParameterHelper;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.hobbit.*;
import org.hobbit.sdk.bsbm.system.SystemAdapter;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static org.hobbit.sdk.CommonConstants.*;
import static org.hobbit.sdk.bsbm.Constants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * @author Pavel Smirnov
 */

public class ExampleBenchmarkTest extends EnvironmentVariablesWrapper {

  private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);

  private RabbitMqDockerizer rabbitMqDockerizer;
  private ComponentsExecutor componentsExecutor;
  private CommandQueueListener commandQueueListener;

  BenchmarkDockerBuilder benchmarkBuilder;
  DataGenDockerBuilder dataGeneratorBuilder;
  TaskGenDockerBuilder taskGeneratorBuilder;
  EvalStorageDockerBuilder evalStorageBuilder;
  SystemAdapterDockerBuilder systemAdapterBuilder;
  EvalModuleDockerBuilder evalModuleBuilder;

  public void init(Boolean useCachedImage) throws Exception {

    benchmarkBuilder = new BenchmarkDockerBuilder(
        new ExampleDockersBuilder<BenchmarkController>(BenchmarkController.class, BENCHMARK_IMAGE_NAME)
            .useCachedImage(useCachedImage));
    dataGeneratorBuilder = new DataGenDockerBuilder(
        new ExampleDockersBuilder<DataGenerator>(DataGenerator.class, DATAGEN_IMAGE_NAME).useCachedImage(useCachedImage)
            .addFileOrFolder("data"));
    taskGeneratorBuilder = new TaskGenDockerBuilder(
        new ExampleDockersBuilder<TaskGenerator>(TaskGenerator.class, TASKGEN_IMAGE_NAME)
            .useCachedImage(useCachedImage));

    evalStorageBuilder = new EvalStorageDockerBuilder(
        new ExampleDockersBuilder<EvalStorage>(EvalStorage.class, EVAL_STORAGE_IMAGE_NAME)
            .useCachedImage(useCachedImage));

    systemAdapterBuilder = new SystemAdapterDockerBuilder(
        new ExampleDockersBuilder<SystemAdapter>(SystemAdapter.class, SYSTEM_IMAGE_NAME)
            .useCachedImage(useCachedImage));
    evalModuleBuilder = new EvalModuleDockerBuilder(
        new ExampleDockersBuilder<EvalModule>(EvalModule.class, EVALMODULE_IMAGE_NAME).useCachedImage(useCachedImage));
  }

  @Test
  public void buildImages() throws Exception {
    int build;
    try {
      Map<String, String> env = System.getenv();
      build = ParameterHelper.getFromEnv(logger, env, "BUILD", 0);
    } catch (IllegalArgumentException invalid) {
      build = 0;
    }
    if (build > 0) {
      init(false);
      benchmarkBuilder.build().prepareImage();
      dataGeneratorBuilder.build().prepareImage();
      taskGeneratorBuilder.build().prepareImage();
      evalStorageBuilder.build().prepareImage();
      evalModuleBuilder.build().prepareImage();
      systemAdapterBuilder.build().prepareImage();
    }
  }

  @Test
  public void checkHealth() throws Exception {
    int docker = 0;
    try {
      Map<String, String> env = System.getenv();
      docker = ParameterHelper.getFromEnv(logger, env, "DOCKER", 0);
    } catch (IllegalArgumentException invalid) {
      docker = 0;
    }
    if (docker != 0) {
      checkHealth(true);
    } else {
      checkHealth(false);
    }
  }

  @Test
  @Ignore
  public void checkHealthDockerized() throws Exception {
    int docker = 0;
    try {
      Map<String, String> env = System.getenv();
      docker = ParameterHelper.getFromEnv(logger, env, "DOCKER", 0);
    } catch (IllegalArgumentException invalid) {
      docker = 0;
    }
    if (docker != 0) {
      checkHealth(true);
    }
  }

  private void checkHealth(Boolean dockerized) throws Exception {

    Boolean useCachedImages = false;
    init(useCachedImages);

    rabbitMqDockerizer = RabbitMqDockerizer.builder().build();

    setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(),
        "session_" + String.valueOf(new Date().getTime()));
    setupBenchmarkEnvironmentVariables(EXPERIMENT_URI, createBenchmarkParameters());
    setupGeneratorEnvironmentVariables(1, 1);
    setupSystemEnvironmentVariables(SYSTEM_URI, createSystemParameters());

    Component benchmarkController;
    Component dataGen;
    Component taskGen;
    Component evalStorage;
    Component systemAdapter;
    Component evalModule;

    if (dockerized) {
      benchmarkController = benchmarkBuilder.build();
      dataGen = dataGeneratorBuilder.build();
      taskGen = taskGeneratorBuilder.build();
      evalStorage = evalStorageBuilder.build();
      evalModule = evalModuleBuilder.build();
      systemAdapter = systemAdapterBuilder.build();
    } else {
      benchmarkController = new BenchmarkController();
      dataGen = new DataGenerator();
      taskGen = new TaskGenerator();
      evalStorage = new EvalStorage();
      systemAdapter = new SystemAdapter();
      evalModule = new EvalModule();
    }

    commandQueueListener = new CommandQueueListener();
    componentsExecutor = new ComponentsExecutor(commandQueueListener, environmentVariables);

    rabbitMqDockerizer.run();

    commandQueueListener.setCommandReactions(new MultipleCommandsReaction(componentsExecutor, commandQueueListener)
        .dataGenerator(dataGen).dataGeneratorImageName(dataGeneratorBuilder.getImageName()).taskGenerator(taskGen)
        .taskGeneratorImageName(taskGeneratorBuilder.getImageName()).evalStorage(evalStorage)
        .evalStorageImageName(evalStorageBuilder.getImageName()).evalModule(evalModule)
        .evalModuleImageName(evalModuleBuilder.getImageName()).systemContainerId(systemAdapterBuilder.getImageName()));

    componentsExecutor.submit(commandQueueListener);
    commandQueueListener.waitForInitialisation();

    componentsExecutor.submit(benchmarkController);
    componentsExecutor.submit(systemAdapter, systemAdapterBuilder.getImageName());

    commandQueueListener.waitForTermination();

    rabbitMqDockerizer.stop();

    Assert.assertFalse(componentsExecutor.anyExceptions());

    // Close all components
    benchmarkController.close();
    dataGen.close();
    taskGen.close();
    evalStorage.close();
    systemAdapter.close();
    evalModule.close();

  }

  public JenaKeyValue createBenchmarkParameters() {
    JenaKeyValue kv = new JenaKeyValue();
    //kv.setValue(BENCHMARK_MODE_INPUT_NAME, BENCHMARK_MODE_DYNAMIC+":10:1");
    kv.setValue(GENERATOR_PC_KEY, GENERATOR_PC_DEFAULT_VALUE);
    kv.setValue(GENERATOR_UD_KEY, GENERATOR_UD_DEFAULT_VALUE);
    kv.setValue(GENERATOR_TC_KEY, GENERATOR_TC_DEFAULT_VALUE);
    kv.setValue(GENERATOR_PPT_KEY, GENERATOR_PPT_DEFAULT_VALUE);
    kv.setValue(TESTDRIVER_WARMUPS_KEY, TESTDRIVER_WARMUPS_DEFAULT_VALUE);
    kv.setValue(TESTDRIVER_RUNS_KEY, TESTDRIVER_RUNS_DEFAULT_VALUE);
    //kv.setValue(TESTDRIVER_SEED_KEY , TESTDRIVER_SEED_DEFAULT_VALUE);

    return kv;
  }

  private static JenaKeyValue createSystemParameters() {
    JenaKeyValue kv = new JenaKeyValue();
    //kv.setValue(BENCHMARK_MODE_INPUT_NAME, BENCHMARK_MODE_DYNAMIC+":10:1");
    kv.setValue(GENERATOR_PC_KEY, GENERATOR_PC_DEFAULT_VALUE);
    kv.setValue(GENERATOR_UD_KEY, GENERATOR_UD_DEFAULT_VALUE);
    kv.setValue(GENERATOR_TC_KEY, GENERATOR_TC_DEFAULT_VALUE);
    kv.setValue(GENERATOR_PPT_KEY, GENERATOR_PPT_DEFAULT_VALUE);
    kv.setValue(TESTDRIVER_WARMUPS_KEY, TESTDRIVER_WARMUPS_DEFAULT_VALUE);
    kv.setValue(TESTDRIVER_RUNS_KEY, TESTDRIVER_RUNS_DEFAULT_VALUE);
    //kv.setValue(TESTDRIVER_SEED_KEY , TESTDRIVER_SEED_DEFAULT_VALUE);

    return kv;
  }

}
