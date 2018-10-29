/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.downloadbook.threadpool;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import zjtech.piczz.downloadbook.SingleBookEntity;
import zjtech.piczz.downloadbook.SinglePictureEntity;
import zjtech.piczz.gs.GlobalSettingEntity;
import zjtech.piczz.gs.GlobalSettingService;

/**
 * Download each pictures
 */
@Component
@Scope("prototype")
@Slf4j
public class DownloadUtil {

  private final GlobalSettingService settingService;

  @Autowired
  public DownloadUtil(GlobalSettingService settingService) {
    this.settingService = settingService;
  }

  private void downloadUsingStream(String urlStr, String filePath) throws IOException {
    //获取下载地址
    URL url = new URL(urlStr);
    //链接网络地址
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty("Referer", "http://www.177piczz.info/html/2018/07/2168507.html");
    connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
    connection.setRequestMethod("GET");
//        connection.setConnectTimeout(5 * 1000);
    connection.setRequestProperty("User-Agent",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36");

    try (BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
         FileOutputStream fis = new FileOutputStream(filePath)) {
      byte[] buffer = new byte[2048];
      int count = 0;
      while ((count = bis.read(buffer, 0, 2048)) != -1) {
        fis.write(buffer, 0, count);
      }
    }
    connection.disconnect();

  }

  public boolean isBookExisted(SingleBookEntity bookEntity) {
    String storagePath = settingService.getOne().get().getStorageDirectory();
    if (!storagePath.endsWith("/")) {
      storagePath += "/";
    }
    String folderName = storagePath + bookEntity.getName();
    Path strPath = Paths.get(folderName);
    return Files.exists(strPath);
  }

  public SinglePictureEntity process(SinglePictureEntity item) throws Exception {
    String url = item.getUrl();
    if (!StringUtils.isBlank(url)) {

      GlobalSettingEntity settingEntity = settingService.getOne().get();
      String storagePath = settingService.getOne().get().getStorageDirectory();
      int retryCount = settingEntity.getNumberOfRetries();

      //generate the local directory
      String folderName = item.getBooKName();
      if (!storagePath.endsWith("/")) {
        storagePath += "/";
      }
      folderName = storagePath + folderName;
      Path strPath = Paths.get(folderName);
      if (Files.notExists(strPath)) {
        Files.createDirectories(strPath);
      }

      //construct the file path
      String picType = url.substring(url.lastIndexOf("."));
      String fileName = folderName + "/" + new StringBuilder().append(item.getSubPageNo())
          .append("-").append(item.getPicIndexInfo()).append(picType).toString();

      log.info("fileName=" + fileName);
      if (Files.exists(Paths.get(fileName))) {
        //continue
        return null;
      }
      log.info("will download pic: " + fileName);

      int count = 0;
      do {
        try {
          //download the pic
          downloadUsingStream(url, fileName);
          break;
        } catch (Exception e) {
          if (count < retryCount) {
            log.info("Retry downloading image: {}", fileName);
            count++;
          } else {
            log.warn("failed to download: {}", url, e);
          }
        }
      } while (count < retryCount);
    }
    return item;
  }

}
