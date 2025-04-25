package com.tenyon.web.manager.cache;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * TopKey
 *
 * @author tenyon
 * @date 2025/4/25
 */
public interface TopK {

    AddResult add(String key, int increment);

    List<Item> list();

    BlockingQueue<Item> expelled();

    void fading();

    long total();
}
