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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import net.easydebug.osexplorer.model.AppModel;
import net.easydebug.osexplorer.service.AmazonS3Service;
import net.easydebug.osexplorer.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Connect Controller
 *
 * @author JiaXiaohei
 */
public class ConnectController implements Initializable {

    private static Logger logger = LoggerFactory.getLogger(ConnectController.class);

    @FXML
    public TextField bucketNameTextField;

    @FXML
    public TextField accessKeyIdTextField;

    @FXML
    public TextField secretKeyTextField;

    @FXML
    public Button closeWindowButton;

    @FXML
    public Button doConnectButton;

    AmazonS3Service amazonS3Service = new AmazonS3Service();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * 点击确定
     */
    @FXML
    private void doConnect() {

        String accessKey = accessKeyIdTextField.getText();
        String secretKey = secretKeyTextField.getText();
        String bucketName = bucketNameTextField.getText();

        final Service redisKeyListService = amazonS3Service.init(accessKey, secretKey, bucketName);
        redisKeyListService.setOnSucceeded(redisKeyListEvent -> {
            AppModel.setStatusText("");
            AppModel.setStatusDouble(0);
            Stage stage = (Stage) doConnectButton.getScene().getWindow();
            stage.close();
        });

    }

    /**
     * 点击关闭
     */
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) closeWindowButton.getScene().getWindow();
        stage.close();
    }

}
