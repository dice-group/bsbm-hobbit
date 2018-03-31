package org.hobbit.sdk.bsbm.helper;

import java.util.*;

public class EasyCircularArrayList<T> {
  private List<T> buffer;
  private int currentIndex;

  public EasyCircularArrayList(List<T> list) {
    buffer = new ArrayList<T>(list);
  }

  public T getNext() {
    T tmp = buffer.get(currentIndex++);
    if (currentIndex >= buffer.size()) {
      currentIndex = 0;
    }
    return tmp;
  }

  public int size() {
    return buffer.size();
  }

}
