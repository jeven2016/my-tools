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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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

  public boolean isBookExisted(String bookName) {
    Path strPath = getBookPath(bookName);
    return Files.exists(strPath);
  }

  public Path getBookPath(String bookName, GlobalSettingEntity settingEntity) {
    String storagePath = settingEntity.getStorageDirectory();
    if (!storagePath.endsWith("/")) {
      storagePath += "/";
    }
    String folderName = storagePath + bookName;
    return Paths.get(folderName);
  }

  public Path getBookPath(String bookName) {
    return getBookPath(bookName, settingService.getOne().get());
  }

  public SinglePictureEntity process(SinglePictureEntity item) throws Exception {
    String url = item.getUrl();
    if (!StringUtils.isEmpty(url)) {

      GlobalSettingEntity settingEntity = settingService.getOne().get();
      int retryCount = settingEntity.getNumberOfRetries();

      Path strPath = getBookPath(item.getBooKName(), settingEntity);
      if (Files.notExists(strPath)) {
        Files.createDirectories(strPath);
      }

      //construct the file path
      String picType = url.substring(url.lastIndexOf("."));
      Path fileName = strPath.resolve(Paths.get(new StringBuilder().append(item.getSubPageNo())
          .append("-").append(item.getPicIndexInfo()).append(picType).toString()));

      log.info("pic fileName=" + fileName);
      if (Files.exists(fileName)) {
        //continue
        return null;
      }
      log.info("will download pic({}), url={} ", item.getBooKName(), url);

      int count = 0;
      do {
        try {
          //download the pic
          downloadUsingStream(url, fileName.toAbsolutePath().toString());
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
