package org.hobbit.sdk.bsbm.benchmark;

import benchmark.testdriver.Query;
import benchmark.testdriver.TestDriver;
import benchmark.testdriver.TestDriverDefaultValues;
import org.apache.log4j.Level;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class BSBMTestDriver extends TestDriver {

  public BSBMTestDriver(String[] args) {
    super(args);
  }

  @Override
  public void run() {
    int qmsPerPeriod = TestDriverDefaultValues.qmsPerPeriod;
    int qmsCounter = 0;
    int periodCounter = 0;
    double periodRuntime = 0;
    BufferedWriter measurementFile = null;
    BufferedWriter queryMixFile = null;
    try {
      measurementFile = new BufferedWriter(new FileWriter("steadystate.tsv"));
      queryMixFile = new BufferedWriter(new FileWriter("queryMix.query"));
    } catch (IOException e) {
      System.err.println("Could not create file steadystate.tsv or queryMix.query");
      System.exit(-1);
    }

    String warmup;
    for (int nrRun = -warmups; nrRun < nrRuns; nrRun++) {
      long startTime = System.currentTimeMillis();
      queryMix.setRun(nrRun);
      while (queryMix.hasNext()) {
        Query next = queryMix.getNext();

        if (nrRun < 0) {
          warmup = "-1|";
        } else {
          warmup = "";
        }

        // Don't run update queries on warm-up //TODO: well yeah
        if (nrRun < 0 && next.getQueryType() == Query.UPDATE_TYPE) {
          queryMix.setCurrent(0, -1.0);
          continue;
        }

        Object[] queryParameters = parameterPool.getParametersForQuery(next);
        next.setParameters(queryParameters);

        try {
          queryMixFile.append(warmup + next.getQueryString().replaceAll("(\\r|\\n|\\r\\n)+", " ") + "\n");
          queryMixFile.flush();
        } catch (IOException e) {
          e.printStackTrace();
          //System.exit(-1);
        }

        if (ignoreQueries[next.getNr() - 1])
          queryMix.setCurrent(0, -1.0);
        else {
          //System.out.println("### Testdriver: fake server call Type: " + next.getQueryType());
          // seems to work
          next.getQueryMix().setCurrent(0, 0.0);
          //server.executeQuery(next, next.getQueryType());
        }
      }

      // Ignore warm-up measures
      if (nrRun >= 0) {
        qmsCounter++;
        periodRuntime += queryMix.getQueryMixRuntime();
      }

      // Write out period data
      if (qmsCounter == qmsPerPeriod) {
        periodCounter++;
        try {
          measurementFile.append(periodCounter + "\t" + periodRuntime + "\n");
          measurementFile.flush();
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(-1);
        }
        periodRuntime = 0;
        qmsCounter = 0;
      }

      //System.out.println(nrRun + ": " + String.format(Locale.US, "%.2f", queryMix.getQueryMixRuntime() * 1000)
      //    + "ms, total: " + (System.currentTimeMillis() - startTime) + "ms");
      queryMix.finishRun();
    }
    logger.log(Level.ALL, printResults(printStuff));

    try {
      FileWriter resultWriter = new FileWriter(xmlResultFile);
      resultWriter.append(printXMLResults(printStuff));
      resultWriter.flush();
      resultWriter.close();
      measurementFile.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static boolean printStuff = false;

  public static void main(String[] argv) {
    DOMConfigurator.configureAndWatch("log4j.xml", 60 * 1000);
    BSBMTestDriver testDriver = new BSBMTestDriver(argv);
    testDriver.init();
    //System.out.println("\nStarting test...\n");
    if (testDriver.multithreading) {
      testDriver.runMT();
      //System.out.println("\n" + testDriver.printResults(printStuff));
    } else if (testDriver.qualification)
      testDriver.runQualification();
    else if (testDriver.rampup)
      testDriver.runRampup();
    else {
      testDriver.run();
      //System.out.println("\n" + testDriver.printResults(printStuff));
    }
  }

}
