/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.downloadbook.threadpool;

import java.util.concurrent.BlockingQueue;
import lombok.extern.slf4j.Slf4j;
import zjtech.piczz.downloadbook.SinglePictureEntity;

@Slf4j
public class DownloadTask implements Runnable {

  private BlockingQueue<SinglePictureEntity> blockingQueue;
  private DownloadUtil util;

  public DownloadTask(BlockingQueue<SinglePictureEntity> blockingQueue, DownloadUtil util) {
    this.blockingQueue = blockingQueue;
    this.util = util;
  }

  @Override
  public void run() {
    SinglePictureEntity pic;
    try {
      log.info("{} is waiting for next picture task", Thread.currentThread().getName());
      log.info("current pool size is {}", blockingQueue.size());
      while ((pic = blockingQueue.take()) != null) {
        doSomething(pic);
      }
    } catch (Exception e) {
      log.error("exit thread", e);
    }
  }

  private void doSomething(SinglePictureEntity pic) {
    try {
      log.info(Thread.currentThread().getName() + ": pic is downloading:" + pic.getUrl());
      util.process(pic);
      log.info(Thread.currentThread().getName() + ": pic is downloaded:" + pic.getUrl());
    } catch (Exception e) {
      log.warn("failed to download picture for" + pic.getUrl(), e);
    }
  }
}
