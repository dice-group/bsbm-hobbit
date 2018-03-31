package org.hobbit.sdk.bsbm.benchmark;

import java.util.Map;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.components.AbstractEvaluationModule;
import org.hobbit.sdk.bsbm.helper.ParameterHelper;
import org.hobbit.sdk.bsbm.helper.QueryEval;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.Lang;

import java.util.concurrent.atomic.AtomicLong;
import java.util.*;
import static org.hobbit.sdk.bsbm.Constants.*;
import java.lang.Math;

public class EvalModule extends AbstractEvaluationModule {
  private static final Logger logger = LoggerFactory.getLogger(EvalModule.class);

  private AtomicLong overallRuntime = new AtomicLong(0L);
  private AtomicLong earliestTimestamp = new AtomicLong(Long.MAX_VALUE);
  private AtomicLong latestTimestamp = new AtomicLong(0L);
  private AtomicLong finishedTasks = new AtomicLong(0L);
  private AtomicLong failedTasks = new AtomicLong(0L);

  private HashMap<String, QueryEval> evalMap;

  private double logOverallRuntime = 0.0;

  // environment parameter
  private int productCount;
  private boolean useUpdateDataset;
  private int updateTransactionCount;
  private int productsPerUpdateTransaction;
  private int runs;
  private int warmupRuns;
  private long seed;

  @Override
  public void init() throws Exception {
    super.init();
    initFromEnv();
    evalMap = new HashMap<String, QueryEval>();
  }

  private void setIfValue(AtomicLong toSet, long update, boolean less) {
    long tmp = toSet.get();
    if (less) {
      while (update < tmp) {
        toSet.compareAndSet(tmp, update);
        tmp = toSet.get();
      }
    } else {
      while (update > tmp) {
        toSet.compareAndSet(tmp, update);
        tmp = toSet.get();
      }
    }
  }

  private void setIfMaxValue(AtomicLong toSet, long update) {
    setIfValue(toSet, update, false);
  }

  private void setIfMinValue(AtomicLong toSet, long update) {
    setIfValue(toSet, update, true);
  }

  /**
   * check timestamps and sort values by querytype
   * @param  expectedData              dummy for querytype
   * @param  receivedData              not really needed
   * @param  taskSentTimestamp         sent timestamp
   * @param  responseReceivedTimestamp received timestamp
   * @throws Exception                 [description]
   */
  @Override
  protected void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp,
      long responseReceivedTimestamp) throws Exception {
    logger.trace("evaluateResponse() receivedData: " + new String(receivedData));
    String queryNr = new String(expectedData); // -1 if warmup
    // Ignore warmup runs
    if (queryNr.equals("-1")) {
      return;
    }
    // evaluate the given response and store the result, e.g., increment internal counters
    long currentET = 0;
    logger.trace("evaluateResponse() QueryNr: " + queryNr);
    // sanity check timestamps
    if (taskSentTimestamp > 0 && responseReceivedTimestamp > 0 && responseReceivedTimestamp >= taskSentTimestamp) {
      currentET = responseReceivedTimestamp - taskSentTimestamp;

      finishedTasks.incrementAndGet();

      setIfMinValue(earliestTimestamp, taskSentTimestamp);
      setIfMaxValue(latestTimestamp, responseReceivedTimestamp);

      if (currentET > 0) {
        logOverallRuntime += Math.log(currentET);
      }

      overallRuntime.addAndGet(currentET);
    } else {
      //failedTasks++;
      currentET = -1;
      failedTasks.incrementAndGet();
      logger.trace(
          "### FAILED TASK --- taskSent: " + taskSentTimestamp + " responcetimestamp: " + responseReceivedTimestamp);
    }

    // ################ HashMap testing
    QueryEval currentQuery = null;
    if ((currentQuery = evalMap.get(queryNr)) == null) {
      currentQuery = new QueryEval();
      evalMap.put(queryNr, currentQuery);
    }
    currentQuery.setCurrentExecutiontime(currentET);
    // ################

    logger.trace("evaluateResponse() for Task with arbitrary number: " + (finishedTasks.get() + failedTasks.get())
        + "\t Runtime: " + overallRuntime.get());
  }

  private void mapEval(String key, QueryEval query, double totalTime, Model model) {
    logger.debug("QueryNr: {} Count: {}, Fail: {}, MinQET/MaxQET: {}/{}, AvgQET: {}, AvgQET(geo.): {}, QpS: {}", key,
        query.getCount(), query.getFailed(), query.getMinQET(), query.getMaxQET(), query.getArithmicAQET(),
        query.getGeometricAQET(), query.getCount() / totalTime);
    String bsbm = ENV_VARIABLES_URI;
    String queryStr = KPI_PREFIX + key;
    String base = bsbm + queryStr;
    double qps = query.getCount() / totalTime;
    qps = (!Double.isNaN(qps)) ? qps : -1.0;
    Property prop = null;

    Resource experimentRes = model.getResource(experimentUri);

    prop = model.createProperty(base + KPI_SUCEESS);
    model.add(experimentRes, prop, model.createTypedLiteral(query.getCount()));

    prop = model.createProperty(base + KPI_FAIL);
    model.add(experimentRes, prop, model.createTypedLiteral(query.getFailed()));

    prop = model.createProperty(base + KPI_MINQET);
    model.add(experimentRes, prop, model.createTypedLiteral(query.getMinQET()));

    prop = model.createProperty(base + KPI_MAXQET);
    model.add(experimentRes, prop, model.createTypedLiteral(query.getMaxQET()));

    prop = model.createProperty(base + KPI_AAVGQET);
    model.add(experimentRes, prop, model.createTypedLiteral(query.getArithmicAQET()));

    prop = model.createProperty(base + KPI_GAVGQET);
    model.add(experimentRes, prop, model.createTypedLiteral(query.getGeometricAQET()));

    prop = model.createProperty(base + KPI_QPS);
    model.add(experimentRes, prop, model.createTypedLiteral(qps));

  }

  @Override
  protected Model summarizeEvaluation() throws Exception {
    logger.debug("summarizeEvaluation()");
    Model model = createDefaultModel();

    double totalTime = (latestTimestamp.get() - earliestTimestamp.get()) / 1000.0;
    long finish = finishedTasks.get();
    long failed = failedTasks.get();
    logger.debug("+++ Overall Time for all: {} Time for all accumulated: {}, All Avg: {}, All AvgGeo: {}", totalTime,
        overallRuntime.longValue(), overallRuntime.longValue() / (double) finish,
        Math.exp(logOverallRuntime / (double) finish));
    logger.debug("+++ All Tasks: " + (finish + failed) + " Finish: " + finish + " Fail: " + failed);

    // All tasks/responsens have been evaluated. Summarize the results,
    // write them into a Jena model and send it to the benchmark controller.
    Resource experimentResource = model.getResource(experimentUri);
    model.add(experimentResource, RDF.type, HOBBIT.Experiment);

    String base = ENV_VARIABLES_URI;
    Property prop = null;
    prop = model.createProperty(base + KPI_GLOBAL_SUCCESS);
    model.add(experimentResource, prop, model.createTypedLiteral(finish));

    prop = model.createProperty(base + KPI_GLOBAL_FAILURE);
    model.add(experimentResource, prop, model.createTypedLiteral(failed));

    prop = model.createProperty(base + KPI_GLOBAL_RUNTIME);
    model.add(experimentResource, prop, model.createTypedLiteral(totalTime));

    prop = model.createProperty(base + KPI_GLOBAL_RUNTIMEACC);
    model.add(experimentResource, prop, model.createTypedLiteral(overallRuntime.longValue() / 1000.0));

    prop = model.createProperty(base + KPI_GLOBAL_AQET);
    model.add(experimentResource, prop, model.createTypedLiteral(overallRuntime.longValue() / (double) finish));

    prop = model.createProperty(base + KPI_GLOBAL_AQETGEO);
    model.add(experimentResource, prop, model.createTypedLiteral(Math.exp(logOverallRuntime / (double) finish)));

    prop = model.createProperty(base + KPI_GLOBAL_QMPH);
    model.add(experimentResource, prop, model.createTypedLiteral(((double) runs / totalTime) * 60 * 60));

    evalMap.forEach((k, v) -> mapEval(k, v, totalTime, model));

    // DEBUG
    RDFDataMgr.write(System.out, model, Lang.TURTLE);

    return model;
  }

  @Override
  public void close() {
    // Free the resources you requested here
    logger.debug("close()");
    // Always close the super class after yours!
    try {
      super.close();
    } catch (Exception e) {

    }
  }

  public void initFromEnv() {
    logger.info("Getting EvalModule's properites from the environment...");

    Map<String, String> env = System.getenv();
    productCount = ParameterHelper.getFromEnv(logger, env, "pc", 0);
    useUpdateDataset = Boolean.parseBoolean(ParameterHelper.getFromEnv(logger, env, "ud", ""));
    updateTransactionCount = ParameterHelper.getFromEnv(logger, env, "tc", 0);
    productsPerUpdateTransaction = ParameterHelper.getFromEnv(logger, env, "ppt", 0);
    runs = ParameterHelper.getFromEnv(logger, env, "runs", 0);
    warmupRuns = ParameterHelper.getFromEnv(logger, env, "warmups", 50);
    seed = ParameterHelper.getFromEnv(logger, env, "seed", 0L);

    logger.debug("######### initFromEnv EvalModule: pc=" + productCount + ", ud=" + useUpdateDataset + ", tc="
        + updateTransactionCount + ", ppt=" + productsPerUpdateTransaction + ", runs=" + runs + " warmups=" + warmupRuns
        + ", seed=" + seed);
  }

}
