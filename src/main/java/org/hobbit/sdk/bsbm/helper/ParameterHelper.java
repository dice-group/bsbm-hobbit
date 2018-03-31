package org.hobbit.sdk.bsbm.helper;

import java.util.Map;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.slf4j.Logger;


/**
 * Yes, to shorten this code you could use generic functions instead of overloading.
 * For certain reasons we didn't do it.
 */
public class ParameterHelper {

  /**
   * A method for loading parameters from the benchmark parameter model
   *
   * @param benchmarkParamModel the model
   * @param logger the logger
   * @param property the property that should be loaded
   * @param defaultValue the default value (to be used in case of an error)
   */
  public static String getProperty(Model benchmarkParamModel, Logger logger, String property, String defaultValue) {
    NodeIterator iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel.getProperty(property));
    if(iterator.hasNext()) {
      return iterator.next().asLiteral().getString();
    } else {
      logger.info("Couldn't get property '" + property + "' from the parameter model. Using '" +
          defaultValue + "' as a default value.");
      return defaultValue;
    }
  }

  /**
   * A method for loading parameters from the benchmark parameter model
   *
   * @param benchmarkParamModel the model
   * @param logger the logger
   * @param property the property that should be loaded
   * @param defaultValue the default value (to be used in case of an error)
   */
  public static double getProperty(Model benchmarkParamModel, Logger logger, String property, double defaultValue) {
    NodeIterator iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel.getProperty(property));
    if(iterator.hasNext()) {
      return iterator.next().asLiteral().getDouble();
    } else {
      logger.info("Couldn't get property '" + property + "' from the parameter model. Using '" +
          defaultValue + "' as a default value.");
      return defaultValue;
    }
  }

  /**
   * A method for loading parameters from the benchmark parameter model
   *
   * @param benchmarkParamModel the model
   * @param logger the logger
   * @param property the property that should be loaded
   * @param defaultValue the default value (to be used in case of an error)
   */
  public static int getProperty(Model benchmarkParamModel, Logger logger, String property, int defaultValue) {
    NodeIterator iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel.getProperty(property));
    if(iterator.hasNext()) {
      return iterator.next().asLiteral().getInt();
    } else {
      logger.info("Couldn't get property '" + property + "' from the parameter model. Using '" +
          defaultValue + "' as a default value.");
      return defaultValue;
    }
  }

  /**
   * A method for loading parameters from the benchmark parameter model
   *
   * @param benchmarkParamModel the model
   * @param logger the logger
   * @param property the property that should be loaded
   * @param defaultValue the default value (to be used in case of an error)
   */
  public static long getProperty(Model benchmarkParamModel, Logger logger, String property, long defaultValue) {
    NodeIterator iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel.getProperty(property));
    if(iterator.hasNext()) {
      return iterator.next().asLiteral().getLong();
    } else {
      logger.info("Couldn't get property '" + property + "' from the parameter model. Using '" +
          defaultValue + "' as a default value.");
      return defaultValue;
    }
  }
  
  /**
   * A method to receive benchmark parameters from environment variables as a "String"
   *
   * @param logger the logger
   * @param env a map of all environment variables
   * @param parameter the property that we want to receive
   * @param paramType a dummy parameter to recognize the type of the property
   */
  public static String getFromEnv(Logger logger, Map<String, String> env, String parameter, String paramType) {
    if (!env.containsKey(parameter)) {
      logger.error("Environment variable '" + parameter + "' is not set. Aborting.");
      throw new IllegalArgumentException("Environment variable '" + parameter + "' is not set. Aborting.");
    } else {
      try {
        return env.get(parameter);
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Couldn't get '" + parameter + "' from the environment. Aborting.", e);
      }
    }
  }

  /**
   * A method to receive benchmark parameters from environment variables as a "double"
   *
   * @param logger the logger
   * @param env a map of all environment variables
   * @param parameter the property that we want to receive
   * @param paramType a dummy parameter to recognize the type of the property
   */
  public static double getFromEnv(Logger logger, Map<String, String> env, String parameter, double paramType) {
    if (!env.containsKey(parameter)) {
      logger.error("Environment variable '" + parameter + "' is not set. Aborting.");
      throw new IllegalArgumentException("Environment variable '" + parameter + "' is not set. Aborting.");
    } else {
      try {
        return Double.parseDouble(env.get(parameter));
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Couldn't get '" + parameter + "' from the environment. Aborting.", e);
      }
    }
  }

  /**
   * A method to receive benchmark parameters from environment variables as an "int"
   *
   * @param logger the logger
   * @param env a map of all environment variables
   * @param parameter the property that we want to receive
   * @param paramType a dummy parameter to recognize the type of the property
   */
  public static int getFromEnv(Logger logger, Map<String, String> env, String parameter, int paramType) {
    if (!env.containsKey(parameter)) {
      logger.error("Environment variable '" + parameter + "' is not set. Aborting.");
      throw new IllegalArgumentException("Environment variable '" + parameter + "' is not set. Aborting.");
    } else {
      try {
        return Integer.parseInt(env.get(parameter));
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Couldn't get '" + parameter + "' from the environment. Aborting.", e);
      }
    }
  }

  /**
   * A method to receive benchmark parameters from environment variables as a "long"
   *
   * @param logger the logger
   * @param env a map of all environment variables
   * @param parameter the property that we want to receive
   * @param paramType a dummy parameter to recognize the type of the property
   */
  public static long getFromEnv(Logger logger, Map<String, String> env, String parameter, long paramType) {
    if (!env.containsKey(parameter)) {
      logger.error("Environment variable '" + parameter + "' is not set. Aborting.");
      throw new IllegalArgumentException("Environment variable '" + parameter + "' is not set. Aborting.");
    } else {
      try {
        return Long.parseLong(env.get(parameter));
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Couldn't get '" + parameter + "' from the environment. Aborting.", e);
      }
    }
  }

}
