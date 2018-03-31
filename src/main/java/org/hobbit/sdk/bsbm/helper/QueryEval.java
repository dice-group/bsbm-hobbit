package org.hobbit.sdk.bsbm.helper;

public class QueryEval {
  private int occurrence;
  private int failedRuns;

  private long overallExecutiontime;
  private double overallExecutiontimeLogSum;

  private long minQET;
  private long maxQET;

  private boolean isValid;
  private boolean untouched;

  public QueryEval() {
    minQET = Long.MAX_VALUE;
    maxQET = 0L;
    occurrence = 0;
    failedRuns = 0;
    overallExecutiontime = 0;
    overallExecutiontimeLogSum = 0.0;
    isValid = false;
    untouched = true;
  }

  private void setMinQET(long check) {
    minQET = (check < minQET) ? check : minQET;
  }

  private void setMaxQET(long check) {
    maxQET = (check > maxQET) ? check : maxQET;
  }

  private void incrementOccurence() {
    occurrence++;
  }

  public void setCurrentExecutiontime(long currentExecutiontime) {
    if (currentExecutiontime < 0) {
      failedRuns++;
      return; // record incorrect Values
    }
    incrementOccurence();
    setMinQET(currentExecutiontime);
    setMaxQET(currentExecutiontime);

    overallExecutiontime += currentExecutiontime;
    overallExecutiontimeLogSum += Math.log(currentExecutiontime);
    if (untouched) {
      untouched = false;
      isValid = true;
    }
    if (Double.isNaN(currentExecutiontime)) {
      isValid = false;
    }
  }

  public long getMinQET() {
    if (isValid) {
      return minQET;
    } else {
      return -1;
    }
  }

  public long getMaxQET() {
    if (isValid) {
      return maxQET;
    } else {
      return -1;
    }
  }

  public double getArithmicAQET() {
    if (isValid) {
      double tmp = overallExecutiontime / (double) occurrence;
      if (!Double.isNaN(tmp)) {
        return tmp;
      }
    }
    return -1;
  }

  public double getGeometricAQET() {
    if (isValid) {
      double tmp = Math.exp((double) overallExecutiontimeLogSum / (double) occurrence);
      if (!Double.isNaN(tmp)) {
        return tmp;
      }
    }
    return -1;
  }

  public long getCount() {
    if (isValid) {
      return occurrence;
    } else {
      return -1;
    }
  }

  public long getFailed() {
    if (isValid) {
      return failedRuns;
    } else {
      return -1;
    }
  }
}
