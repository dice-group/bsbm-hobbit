package org.hobbit.sdk.bsbm.benchmark;

import benchmark.generator.Generator;
import org.hobbit.sdk.bsbm.helper.ByteHelper;
import org.hobbit.sdk.bsbm.helper.EasyCircularArrayList;
import org.hobbit.sdk.bsbm.helper.ParameterHelper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import org.hobbit.core.components.AbstractDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.hobbit.sdk.bsbm.Constants.*;

import java.io.IOException;
import java.util.*;
import java.nio.ByteBuffer;
import java.lang.StringBuffer;

public class DataGenerator extends AbstractDataGenerator {
  private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);

  // environment parameter
  private int productCount;
  private Boolean useUpdateDataset;
  private int updateTransactionCount;
  private int productsPerUpdateTransaction;
  private int runs;
  private int warmupRuns;
  private long seed;

  @Override
  public void init() throws Exception {
    // Always init the super class first!
    super.init();
    logger.debug("Init()");
    // Your initialization code comes here...
    initFromEnv(); // load parameters from environment
    startBSBMGenerator();
    startBSBMTestDriver();
  }

  private EasyCircularArrayList<String> loadQueryMixIndex(List<String> files) throws IOException {
    logger.debug("loadQueryMixIndex()");
    List<String> indexList = new ArrayList<String>();
    for (String filename : files) {
      logger.debug("loadQueryMixIndex() for filenames");
      String prefix = "";
      logger.debug("filename:" + filename.substring(filename.indexOf("=")));
      String rawPrefix = filename.substring(filename.indexOf('/') + 1).toLowerCase();
      // exception for bi - businessIntelligence
      if (rawPrefix.equals("bi")) {
        rawPrefix = "business";
      }
      logger.debug("rawprefix:" + rawPrefix);
      prefix = rawPrefix.substring(0, 1).toUpperCase() + rawPrefix.substring(1);
      logger.debug("prefix:" + prefix);

      BufferedReader reader = new BufferedReader(
          new FileReader(new File(filename.substring(filename.indexOf("=") + 1) + "/querymix.txt")));
      String tmpString = "";
      String line = "";
      while ((line = reader.readLine()) != null) {
        tmpString += " " + line;
      }
      String[] tmpArray = tmpString.substring(1).split(" ");
      for (String tmp : tmpArray) {
        indexList.add(prefix + tmp);
      }
      reader.close();
    }
    return new EasyCircularArrayList<String>(indexList);
  }

  @Override
  protected void generateData() throws Exception {
    // Create your data inside this method. You might want to use the
    // id of this data generator and the number of all data generators
    // running in parallel.
    int dataGeneratorId = getGeneratorId();
    int numberOfGenerators = getNumberOfGenerators();

    logger.debug("generateData() Using " + numberOfGenerators + " Generators. I am Number " + dataGeneratorId);

    logger.debug("Working Dir: " + System.getProperty("user.dir"));

    BufferedReader reader = null;
    BufferedReader queryMixReader = null;
    BufferedReader useCaseReader = null;

    try {
      File queryMixFile = new File("queryMix.query");
      queryMixReader = new BufferedReader(new FileReader(queryMixFile));

      String queryMixLine = "";
      List<String> queryMixList = new ArrayList<String>();
      logger.debug("try to read usecasefile");
      String useCaseFile = "usecases/explore/sparql.txt";
      if (useUpdateDataset) {
        useCaseFile = "usecases/exploreAndUpdate/sparql.txt";
      }
      useCaseReader = new BufferedReader(new FileReader(useCaseFile));
      logger.debug("try to read usecasefile");
      while ((queryMixLine = useCaseReader.readLine()) != null) {
        queryMixList.add(queryMixLine);
      }

      EasyCircularArrayList<String> ringIndex = loadQueryMixIndex(queryMixList);
      String queryLine;
      while ((queryLine = queryMixReader.readLine()) != null) {
        if (!queryLine.startsWith("-1|")) { // not a warmup anymore
          queryLine = ringIndex.getNext() + "|" + queryLine;
        }
        logger.trace("DEBUG RINGBUFFER: " + queryLine.substring(0, queryLine.indexOf('|')));
        logger.trace("sendDataToTaskGenerator()->{}", queryLine);
        sendDataToTaskGenerator(queryLine.getBytes());
      }

      // to systemadapter -->
      File file = new File("dataset.nt");
      if (useUpdateDataset) {
        logger.debug("++ doing explore and update - using dataset_update");
        file = new File("dataset_update.nt");
      }
      reader = new BufferedReader(new FileReader(file));

      String graphUri = GRAPH_URI;
      Integer graphUriLength = graphUri.length();
      // send init stuff to systemadapter to adhere to mocha api
      StringBuffer bulk = new StringBuffer("");
      String line = "";
      int totalMsg = 0;
      while (line != null) {
        bulk = new StringBuffer("");
        while ((line = reader.readLine()) != null) {
          bulk.append(line);
          if (bulk.length() * 2 > 1024 * 1024 * 1024) { // 1GiB = 1024 MiB = 1024^2 KiB = 1024^3 Byte
            // send bulk and start a new one or something
            break;
          }
        }

        graphUri = GRAPH_URI + totalMsg;
        graphUriLength = graphUri.length();
        byte[] uriLength = ByteHelper.intToByteArray(graphUriLength);
        byte[] uri = graphUri.getBytes();
        byte[] bulkByte = bulk.toString().getBytes();
        ByteBuffer buf = ByteBuffer.allocate(uriLength.length + uri.length + bulkByte.length);
        buf.put(uriLength);
        buf.put(uri);
        buf.put(bulkByte);
        logger.trace("sendDataToSystemAdapter()->{}->{}", uriLength, new String(buf.array()));
        sendDataToSystemAdapter(buf.array());
        totalMsg++;
      }
      ByteBuffer bulkFinishCommand = ByteBuffer.allocate(5);
      bulkFinishCommand.put(ByteHelper.intToByteArray(totalMsg));
      bulkFinishCommand.put((byte) 1);

      sendToCmdQueue((byte) 151, bulkFinishCommand.array());
    } catch (IOException e) {
      logger.debug(e.getMessage());
      e.printStackTrace();
    } finally {
      try {
        reader.close();
        queryMixReader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  @Override
  public void close() throws IOException {
    // Free the resources you requested here
    logger.debug("close()");
    // Always close the super class after yours!
    super.close();
  }

  /*
   * Uses the given generator from BSBM sourcecode to create test data.
   */
  private void startBSBMGenerator() {
    // TODO remove debug print
    logger.debug("##### start of generation");
    if (useUpdateDataset) {
      String[] arguments = { "-pc", Integer.toString(productCount), "-ud", "-tc",
          Integer.toString(updateTransactionCount), "-ppt", Integer.toString(productsPerUpdateTransaction) };
      logger.debug("########## startBSBMGenerator Arguments: " + Arrays.toString(arguments));
      Generator.main(arguments);
    } else {
      String[] arguments = { "-pc", Integer.toString(productCount) };
      logger.debug("########## startBSBMGenerator Arguments: " + Arrays.toString(arguments));
      Generator.main(arguments);
    }
    logger.debug("##### end of generation");
  }

  /*
   *
   */
  private void startBSBMTestDriver() {
    logger.debug("##### start of BSBMTestDriver");
    if (useUpdateDataset) {
      String[] arguments = { "http://localhost:3030/test/query", "-runs", Integer.toString(runs), "-w",
          Integer.toString(warmupRuns), "-seed", Long.toString(seed), "-udataset", "dataset_update.nt", "-u",
          "http://localhost:3030/test/query", "-ucf", "usecases/exploreAndUpdate/sparql.txt" };
      BSBMTestDriver.main(arguments);
    } else {
      String[] arguments = { "http://localhost:3030/test/query", "-runs", Integer.toString(runs), "-w",
          Integer.toString(warmupRuns), "-seed", Long.toString(seed) };
      BSBMTestDriver.main(arguments);
    }
    logger.debug("##### end of BSBMTestDriver");
  }

  public void initFromEnv() {
    logger.info("Getting Data Generator's properites from the environment...");

    Map<String, String> env = System.getenv();
    productCount = ParameterHelper.getFromEnv(logger, env, "pc", 0);
    useUpdateDataset = Boolean.parseBoolean(ParameterHelper.getFromEnv(logger, env, "ud", ""));
    updateTransactionCount = ParameterHelper.getFromEnv(logger, env, "tc", 0);
    productsPerUpdateTransaction = ParameterHelper.getFromEnv(logger, env, "ppt", 0);
    runs = ParameterHelper.getFromEnv(logger, env, "runs", 0);
    seed = ParameterHelper.getFromEnv(logger, env, "seed", 0L);
    warmupRuns = ParameterHelper.getFromEnv(logger, env, "warmups", 50);

    logger.debug("######### initFromEnv DataGen: pc=" + productCount + ", ud=" + useUpdateDataset + ", tc="
        + updateTransactionCount + ", ppt=" + productsPerUpdateTransaction + ", runs=" + runs + ", warmups="
        + warmupRuns + ", seed=" + seed);
  }

}
