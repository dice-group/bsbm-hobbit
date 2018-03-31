package org.hobbit.sdk.bsbm;

/**
 * @author Pavel Smirnov
 */

public class Constants {

  public static String GIT_REPO_PATH = "git.project-hobbit.eu:4567/";
  //public static String GIT_REPO_PATH = "";
  //public static String PROJECT_NAME = "sdk-example-benchmark/";
  public static String PROJECT_NAME = "bsbm/berlin-sparql-benchmark/";

  //use these constants within BenchmarkController
  public static final String BENCHMARK_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "benchmark-controller";
  public static final String DATAGEN_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "datagen";
  public static final String TASKGEN_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "taskgen";
  public static final String EVAL_STORAGE_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "eval-storage";
  public static final String EVALMODULE_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "eval-module";
  public static final String SYSTEM_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "system-adapter";

  public static final String BENCHMARK_URI = "http://project-hobbit.eu/" + PROJECT_NAME;
  public static final String SYSTEM_URI = "http://project-hobbit.eu/" + PROJECT_NAME + "system";

  public static final String SDK_BUILD_DIR_PATH = "."; //build directory, temp docker file will be created there
  public static final String SDK_JAR_FILE_PATH = "target/sdk-berlin-sparql-benchmark-1.0.jar"; //should be packaged will all dependencies (via 'mvn package -DskipTests=true' command)
  public static final String SDK_WORK_DIR_PATH = "/usr/src/" + PROJECT_NAME;

  // parameters
  public static final String ENV_VARIABLES_URI = "http://project-hobbit.eu/berlin-sparql-benchmark/";

  public static final String GENERATOR_PC_KEY = ENV_VARIABLES_URI + "pc";
  public static final String GENERATOR_UD_KEY = ENV_VARIABLES_URI + "ud";
  public static final String GENERATOR_TC_KEY = ENV_VARIABLES_URI + "tc";
  public static final String GENERATOR_PPT_KEY = ENV_VARIABLES_URI + "ppt";
  public static final String TESTDRIVER_RUNS_KEY = ENV_VARIABLES_URI + "runs";
  public static final String TESTDRIVER_WARMUPS_KEY = ENV_VARIABLES_URI + "w";
  public static final String TESTDRIVER_SEED_KEY = ENV_VARIABLES_URI + "seed";

  // parameter defaults
  public static final int GENERATOR_PC_DEFAULT_VALUE = 10; // scale factor: number of products
  public static final String GENERATOR_UD_DEFAULT_VALUE = "true"; // generate & use update dataset
  public static final int GENERATOR_TC_DEFAULT_VALUE = 10; // number of update transactions
  public static final int GENERATOR_PPT_DEFAULT_VALUE = 1; // nr of products per update transaction
  public static final int TESTDRIVER_RUNS_DEFAULT_VALUE = 50; // number of query mix runs
  public static final int TESTDRIVER_WARMUPS_DEFAULT_VALUE = 50; // number of warm up runs
  public static final long TESTDRIVER_SEED_DEFAULT_VALUE = 808080L; // seed for the testdriver

  // KPI name parts
  public static final String KPI_PREFIX = "qType";
  public static final String KPI_SUCEESS = "Success";
  public static final String KPI_FAIL = "Fail";
  public static final String KPI_MINQET = "MinQET";
  public static final String KPI_MAXQET = "MaxQET";
  public static final String KPI_AAVGQET = "AvgQET";
  public static final String KPI_GAVGQET = "AvgQETgeo";
  public static final String KPI_QPS = "QueriesPerSecond";

  public static final String KPI_GLOBAL_SUCCESS = "overallSuccess";
  public static final String KPI_GLOBAL_FAILURE = "overallFailure";
  public static final String KPI_GLOBAL_RUNTIME = "overallRuntime";
  public static final String KPI_GLOBAL_RUNTIMEACC = "overallRuntimeAcc";
  public static final String KPI_GLOBAL_AQET = "overallAQET";
  public static final String KPI_GLOBAL_AQETGEO = "overallAQETgeo";
  public static final String KPI_GLOBAL_QMPH = "queryMixPerHour";

  public static final String GRAPH_URI = ENV_VARIABLES_URI + "kpi/data0";
}
