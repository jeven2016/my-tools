/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.downloadbook.threadpool;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.piczz.downloadbook.SinglePictureEntity;
import zjtech.piczz.gs.GlobalSettingEntity;
import zjtech.piczz.gs.GlobalSettingService;

@Component
@Slf4j
public class DownloadingThreadPool {

  private volatile boolean isRunning;

  private ReentrantLock lock = new ReentrantLock();

  private final DownloadUtil util;

  private ExecutorService executorService;

  private final GlobalSettingService globalSettingService;

  private static final int capacity = 1000;

  private BlockingQueue<SinglePictureEntity> blockingQueue = new ArrayBlockingQueue(capacity);

  @Autowired
  public DownloadingThreadPool(DownloadUtil util, GlobalSettingService globalSettingService) {
    this.util = util;
    this.globalSettingService = globalSettingService;
  }

  public void run() {
    if (isRunning) {
      return;
    }

    lock.lock();
    try {
      if (isRunning) {
        return;
      }
      int threadCount = initThreadCount();
      log.info("thread count is {}", threadCount);
      executorService = Executors.newFixedThreadPool(threadCount);
      for (int i = 0; i < threadCount; i++) {
        DownloadTask downloadTask = new DownloadTask(blockingQueue, util);
        executorService.submit(downloadTask);
      }
      isRunning = true;
    } finally {
      lock.unlock();
    }
  }

  private int initThreadCount() {
    GlobalSettingEntity settingEntity = globalSettingService.getOne()
        .orElseGet(GlobalSettingEntity::new);

    return settingEntity.getDownloadThreadCount();
  }

  public void addPictures(Collection<SinglePictureEntity> pictureEntities) {

    try {
      for (SinglePictureEntity singlePictureEntity : pictureEntities) {
        blockingQueue.put(singlePictureEntity);
      }
    } catch (Exception e) {
      log.info("cannot add pictures to queue", e);
    }
  }

  public int getPoolSize() {
    return blockingQueue.size();
  }

  public int getCapacity() {
    return capacity;
  }

  public int getThreadCount() {
    return initThreadCount();
  }
}
