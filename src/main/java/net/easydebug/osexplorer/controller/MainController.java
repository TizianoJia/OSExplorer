/*
 * Copyright 2019. easyDebug.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.easydebug.osexplorer.controller;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.easydebug.osexplorer.MainApp;
import net.easydebug.osexplorer.model.AppModel;
import net.easydebug.osexplorer.model.FileTableModel;
import net.easydebug.osexplorer.service.AmazonS3Service;
import net.easydebug.osexplorer.util.AppConstants;
import net.easydebug.osexplorer.util.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static javafx.collections.FXCollections.observableArrayList;

/**
 * MainView Controller
 *
 * @author JiaXiaohei
 */
public class MainController implements Initializable {

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label statusLabel;

    @FXML
    private TableView<FileTableModel> fileTableView;

    @FXML
    private TableColumn<FileTableModel, String> fileNameTableColumn;

    @FXML
    private TableColumn<FileTableModel, String> lastModifiedTableColumn;

    @FXML
    private TableColumn<FileTableModel, String> storageClassTableColumn;

    @FXML
    private TableColumn<FileTableModel, Number> sizeTableColumn;

    @FXML
    private Label pathTextLabel;

    AmazonS3Service amazonS3Service = new AmazonS3Service();

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppModel.statusTextProperty().addListener((observable, oldValue, newValue) -> statusLabel.setText(newValue));
        AppModel.statusDoubleProperty().addListener((observable, oldValue, newValue) -> progressBar.setProgress(newValue.doubleValue()));

        fileNameTableColumn.setCellValueFactory(cellValue -> cellValue.getValue().fileNameProperty());
        lastModifiedTableColumn.setCellValueFactory(cellValue -> {
            ReadOnlyStringProperty cell = new SimpleStringProperty();
            if (null != cellValue.getValue().lastModifiedProperty().get()) {
                cell = new SimpleStringProperty(formatter.format(cellValue.getValue().lastModifiedProperty().get()));
            }
            return cell;
        });
        storageClassTableColumn.setCellValueFactory(cellValue -> cellValue.getValue().storageClassProperty());
        sizeTableColumn.setCellValueFactory(cellValue -> cellValue.getValue().sizeProperty());
        fileTableView.itemsProperty().bind(AppModel.fileTableModelListProperty());
        fileTableView.setRowFactory(tv -> {
            TableRow<FileTableModel> tableRow = new TableRow<>();

            final ContextMenu tableContextMenu = new ContextMenu();
            MenuItem downloadMenuItem = new MenuItem("下载...");
            downloadMenuItem.setOnAction(event -> {
                FileTableModel fileTableModel = tableRow.getItem();

                FileChooser fileChooser = new FileChooser();
                if (fileTableModel.getFileName().indexOf(".") != -1) {
                    fileChooser.setInitialFileName(fileTableModel.getFileName().substring(fileTableModel.getFileName().lastIndexOf("/") + 1, fileTableModel.getFileName().lastIndexOf(".")));
                    String extensions = fileTableModel.getFileName().substring(fileTableModel.getFileName().lastIndexOf(".") + 1);
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter(extensions.toUpperCase() + " (*." + extensions.toUpperCase() + ", *." + extensions.toLowerCase() + ")", "*." + extensions.toLowerCase(), "*." + extensions.toUpperCase()),
                            new FileChooser.ExtensionFilter("All Files", "*.*")
                    );
                } else {
                    fileChooser.setInitialFileName(fileTableModel.getFileName().substring(fileTableModel.getFileName().lastIndexOf("/") + 1));
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("All Files", "*.*")
                    );
                }

                File file = fileChooser.showSaveDialog(MainApp.primaryStage);
                if (null != file) {
                    final Service downloadFileService = amazonS3Service.downloadFile(AppConstants.s3, AppConstants.bucketName, fileTableModel.getFileName(), file.getPath());
                    downloadFileService.setOnSucceeded(dataListEvent -> {
                        AppModel.setStatusText("");
                        AppModel.setStatusDouble(0);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("文件下载完成");
                        alert.setHeaderText("文件下载完成");

                        alert.showAndWait();
                    });
                }
            });
            MenuItem deleteMenuItem = new MenuItem("删除...");
            deleteMenuItem.setOnAction(event -> {
                FileTableModel fileTableModel = tableRow.getItem();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("确认删除");
                alert.setHeaderText("确认删除文件:\n" + fileTableModel.getFileName() + "？");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    final Service deleteFileService = amazonS3Service.deleteFile(AppConstants.s3, AppConstants.bucketName, fileTableModel.getFileName());
                    deleteFileService.setOnSucceeded(dataListEvent -> {
                        String path = pathTextLabel.getText();
                        flushTableData(Utils.isNotNull(path) ? path : "");
                        AppModel.setStatusText("");
                        AppModel.setStatusDouble(0);
                        Alert deleteAlert = new Alert(Alert.AlertType.INFORMATION);
                        deleteAlert.setTitle("文件删除完成");
                        deleteAlert.setHeaderText("文件删除完成");
                        deleteAlert.showAndWait();
                    });
                } else {

                }
            });

            MenuItem renameMenuItem = new MenuItem("重命名...");
            renameMenuItem.setOnAction(event -> {

                FileTableModel fileTableModel = tableRow.getItem();
                TextInputDialog dialog = new TextInputDialog(fileTableModel.getFileName());
                dialog.setTitle("重命名");
                dialog.setHeaderText("重命名文件名:\n" + fileTableModel.getFileName() + "？");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    final Service renameFileService = amazonS3Service.renameFile(AppConstants.s3, AppConstants.bucketName, fileTableModel.getFileName(), result.get());
                    renameFileService.setOnSucceeded(dataListEvent -> {
                        String path = pathTextLabel.getText();
                        flushTableData(Utils.isNotNull(path) ? path : "");
                        AppModel.setStatusText("");
                        AppModel.setStatusDouble(0);
                        Alert renameAlert = new Alert(Alert.AlertType.INFORMATION);
                        renameAlert.setTitle("文件重命名完成");
                        renameAlert.setHeaderText("文件重命名完成");
                        renameAlert.showAndWait();
                    });
                }

            });

            tableRow.setOnMouseClicked(event -> {

                if (!tableRow.isEmpty()) {
                    FileTableModel fileTableModel = tableRow.getItem();

                    if (event.getClickCount() == 2) {
                        if (fileTableModel.isDirectory()) {
                            flushTableData(fileTableModel.getFileName());
                        }
                    }

                    MouseButton button = event.getButton();
                    //右键点击
                    if (button == MouseButton.SECONDARY) {
                        if (!fileTableModel.isDirectory()) {
                            tableContextMenu.getItems().addAll(downloadMenuItem, deleteMenuItem, renameMenuItem);
                            tableRow.setContextMenu(tableContextMenu);

                        }
                    }
                }
            });

            return tableRow;
        });

    }

    @FXML
    void createConnect(ActionEvent event) throws IOException {
        Parent createConnectParent = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/ConnectView.fxml"));
        Stage createConnectStage = new Stage();
        createConnectStage.initModality(Modality.APPLICATION_MODAL);
        createConnectStage.setTitle("登录");
        createConnectStage.setScene(new Scene(createConnectParent));
        createConnectStage.setOnHidden(eventOnHidden -> {
            flushTableData(null);
        });
        createConnectStage.show();
    }

    /**
     * 向上
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void upWard(ActionEvent event) throws IOException {
        String path = pathTextLabel.getText();
        if (null != path && path.length() > 0) {
            path = path.substring(0, path.length() - 1);
            if (path.contains("/")) {
                path = path.substring(0, path.lastIndexOf("/") + 1);
            } else {
                path = "";
            }
            flushTableData(path);
        }
    }

    /**
     * 获取数据
     *
     * @param prefix
     */
    public void flushTableData(String prefix) {
        final Service<List<FileTableModel>> dataListService = amazonS3Service.getDataList(AppConstants.s3, AppConstants.bucketName, prefix);
        dataListService.setOnSucceeded(dataListEvent -> {
            List<FileTableModel> fileTableModelList = dataListService.getValue();
            AppModel.setFileTableModelList(observableArrayList(fileTableModelList));
            AppModel.setStatusText("");
            AppModel.setStatusDouble(0);
        });
        pathTextLabel.setText(prefix);
    }

    /**
     * 上传文件
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void uploadFile(ActionEvent event) throws IOException {

        if (null != AppConstants.s3) {
            String path = pathTextLabel.getText();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择文件：");
            File file = fileChooser.showOpenDialog(MainApp.primaryStage);

            String finalPath = Utils.isNotNull(path) ? path : "";
            final Service uploadFileService = amazonS3Service.uploadFile(AppConstants.s3, AppConstants.bucketName, finalPath, file);
            uploadFileService.setOnSucceeded(dataListEvent -> {
                flushTableData(finalPath);
                AppModel.setStatusText("");
                AppModel.setStatusDouble(0);
                Alert uploadAlert = new Alert(Alert.AlertType.INFORMATION);
                uploadAlert.setTitle("文件上传完成");
                uploadAlert.setHeaderText("文件上传完成");
                uploadAlert.showAndWait();
            });
        } else {
            Alert deleteAlert = new Alert(Alert.AlertType.ERROR);
            deleteAlert.setTitle("未登陆");
            deleteAlert.setHeaderText("未登陆");
            deleteAlert.showAndWait();
        }

    }
}
