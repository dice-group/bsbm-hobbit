package org.hobbit.sdk.bsbm.benchmark;

import org.hobbit.core.components.AbstractTaskGenerator;
import org.hobbit.sdk.bsbm.helper.ByteHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;

import java.io.IOException;

public class TaskGenerator extends AbstractTaskGenerator {
  private static final Logger logger = LoggerFactory.getLogger(TaskGenerator.class);

  @Override
  public void init() throws Exception {
    // Always init the super class first!
    super.init();
    logger.debug("Init()");
    // Your initialization code comes here...
  }

  @Override
  protected void generateTask(byte[] data) throws Exception {
    String dataString = new String(data);
    logger.trace("generateTask()->{}", dataString);
    // Create tasks based on the incoming data inside this method.
    // You might want to use the id of this task generator and the
    // number of all task generators running in parallel.
    int dataGeneratorId = getGeneratorId();
    int numberOfGenerators = getNumberOfGenerators();

    logger.trace("generateTask() Using " + numberOfGenerators + " Generators. I am Number " + dataGeneratorId);

    // Create an ID for the task
    String taskId = getNextTaskId();

    // Create the task and the expected answer // don't do that we are a dummy
    //String taskDataStr = "task_"+taskId+"_"+dataString;
    //String expectedAnswerDataStr = "result_"+taskId;

    // Send the task (dummy-data) to the system (and store the timestamp)
    long timestamp = System.currentTimeMillis();

    String queryNr = dataString.substring(0, dataString.indexOf('|'));
    String queryData = dataString.substring(dataString.indexOf('|') + 1);

    logger.trace("sendTaskToSystemAdapter({})->{}", taskId, queryData);
    byte[] queryBytes = queryData.getBytes();
    byte[] queryLen = ByteHelper.intToByteArray(queryData.length());
    ByteBuffer buf = ByteBuffer.allocate(queryLen.length + queryBytes.length);
    buf.put(queryLen);
    buf.put(queryBytes);
    sendTaskToSystemAdapter(taskId, buf.array());

    // Send the expected answer to the evaluation store // again dummy so we send query type number instead
    logger.trace("sendTaskToEvalStorage({})->{}", taskId, queryNr);
    sendTaskToEvalStorage(taskId, timestamp, queryNr.getBytes());
  }

  @Override
  public void close() throws IOException {
    // Free the resources you requested here
    logger.debug("close()");
    // Always close the super class after yours!
    super.close();
  }

}
