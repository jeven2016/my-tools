/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.util.converter.NumberStringConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import zjtech.modules.common.AbstractController;
import zjtech.piczz.gs.GlobalSettingEntity;
import zjtech.piczz.gs.GlobalSettingService;

@Component
@Slf4j
@Lazy
public class GlobalSettingController extends AbstractController {

  private final GlobalSettingService settingService;
  @FXML
  public TextField homeUrlInput;
  @FXML
  public TextField downloadThreadCount;
  @FXML
  public TextField storageDirctory;
  @FXML
  public TextField retries;
  @FXML
  public TextField downloadTimeout;
  @FXML
  private Button saveBtn;
  private GlobalSettingModal modal = new GlobalSettingModal();

  @Autowired
  public GlobalSettingController(GlobalSettingService settingService) {
    this.settingService = settingService;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    //using data binding
    modal.clear();
    homeUrlInput.textProperty().bindBidirectional(modal.homeUrlProperty());
    downloadThreadCount.textProperty().bindBidirectional(modal.downloadThreadCountProperty());
    storageDirctory.textProperty().bindBidirectional(modal.storageDirectoryProperty());
    retries.textProperty().bindBidirectional(modal.numberOfRetriesProperty());

    //string <-> integer converter
    downloadTimeout.textProperty()
        .bindBidirectional(modal.downloadTimeoutProperty(), new NumberStringConverter());

    Optional<GlobalSettingEntity> optional = settingService.getOne();
    if (optional.isPresent()) {
      GlobalSettingEntity settingEntity = optional.get();
      updateModal(settingEntity);
    }
  }

  private void updateModal(GlobalSettingEntity settingEntity) {
    modal.idProperty().set(settingEntity.getId());
    modal.homeUrlProperty().set(settingEntity.getHomeUrl());
    modal.downloadThreadCountProperty().set(settingEntity.getDownloadThreadCount() + "");
    modal.storageDirectoryProperty().set(settingEntity.getStorageDirectory());
    modal.numberOfRetriesProperty().set(settingEntity.getNumberOfRetries() + "");
    modal.downloadTimeoutProperty().set(settingEntity.getDownloadTimeout());
  }

  public void save(ActionEvent actionEvent) {
    GlobalSettingEntity globalSettingEntity = new GlobalSettingEntity();
    globalSettingEntity.setId(modal.getId());
    globalSettingEntity.setHomeUrl(modal.getHomeUrl());
    globalSettingEntity.setDownloadThreadCount(Integer.parseInt(modal.getDownloadThreadCount()));
    globalSettingEntity.setDownloadTimeout(modal.getDownloadTimeout());
    globalSettingEntity.setStorageDirectory(modal.getStorageDirectory());
    globalSettingEntity.setNumberOfRetries(Integer.parseInt(modal.getNumberOfRetries()));

    GlobalSettingEntity newEntity = settingService.saveOrUpdate(globalSettingEntity);
    updateModal(newEntity);
  }

  private static class GlobalSettingModal {

    private SimpleLongProperty id = new SimpleLongProperty(0);
    private SimpleStringProperty homeUrl = new SimpleStringProperty();
    private SimpleStringProperty downloadThreadCount = new SimpleStringProperty();
    private SimpleIntegerProperty downloadTimeout = new SimpleIntegerProperty(5000);
    private SimpleStringProperty storageDirectory = new SimpleStringProperty();
    private SimpleStringProperty numberOfRetries = new SimpleStringProperty();

    public void clear() {
      setHomeUrl(null);
      setDownloadThreadCount(null);
      setStorageDirectory(null);
      setNumberOfRetries(null);
    }

    public int getDownloadTimeout() {
      return downloadTimeout.get();
    }

    public void setDownloadTimeout(int downloadTimeout) {
      this.downloadTimeout.set(downloadTimeout);
    }

    public SimpleIntegerProperty downloadTimeoutProperty() {
      return downloadTimeout;
    }

    public long getId() {
      return id.get();
    }

    public void setId(long id) {
      this.id.set(id);
    }

    public SimpleLongProperty idProperty() {
      return id;
    }

    public String getHomeUrl() {
      return homeUrl.get();
    }

    public void setHomeUrl(String homeUrl) {
      this.homeUrl.set(homeUrl);
    }

    public SimpleStringProperty homeUrlProperty() {
      return homeUrl;
    }

    public String getDownloadThreadCount() {
      return downloadThreadCount.get();
    }

    public void setDownloadThreadCount(String downloadThreadCount) {
      this.downloadThreadCount.set(downloadThreadCount);
    }

    public SimpleStringProperty downloadThreadCountProperty() {
      return downloadThreadCount;
    }

    public String getStorageDirectory() {
      return storageDirectory.get();
    }

    public void setStorageDirectory(String storageDirectory) {
      this.storageDirectory.set(storageDirectory);
    }

    public SimpleStringProperty storageDirectoryProperty() {
      return storageDirectory;
    }

    public String getNumberOfRetries() {
      return numberOfRetries.get();
    }

    public void setNumberOfRetries(String numberOfRetries) {
      this.numberOfRetries.set(numberOfRetries);
    }

    public SimpleStringProperty numberOfRetriesProperty() {
      return numberOfRetries;
    }
  }

}
