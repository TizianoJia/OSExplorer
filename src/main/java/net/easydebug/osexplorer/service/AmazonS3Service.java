package net.easydebug.osexplorer.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import net.easydebug.osexplorer.model.AppModel;
import net.easydebug.osexplorer.model.FileTableModel;
import net.easydebug.osexplorer.util.AppConstants;
import net.easydebug.osexplorer.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JiaXiaohei
 */
public class AmazonS3Service {

    /**
     * 初始化Amazon S3
     *
     * @param accessKey
     * @param secretKey
     * @return
     */
    public Service init(String accessKey, String secretKey, String bucketName) {

        Service initAmazonS3Service = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                                .withCredentials(
                                        new AWSStaticCredentialsProvider(
                                                new BasicAWSCredentials(accessKey, secretKey)
                                        )
                                )
                                .withRegion(Regions.CN_NORTH_1).build();
                        AppConstants.bucketName = bucketName;
                        AppConstants.serviceType = "AmazonS3";
                        AppConstants.s3 = s3;
                        return null;
                    }
                };
            }
        };
        initAmazonS3Service.setOnRunning(onRunningEvent -> {
            AppModel.setStatusText("初始化中...");
            AppModel.setStatusDouble(-1);
        });
        initAmazonS3Service.setOnFailed(onFailedEvent -> {
            AppModel.setStatusText("初始化失败");
            AppModel.setStatusDouble(0);
            initAmazonS3Service.getException().printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("初始化失败");
            alert.setHeaderText("Amazon S3 初始化失败");
            alert.showAndWait();
        });

        initAmazonS3Service.start();

        return initAmazonS3Service;
    }

    public Service<List<FileTableModel>> getDataList(AmazonS3 s3, String bucketName, String prefix) {

        Service<List<FileTableModel>> redisInfoService = new Service<List<FileTableModel>>() {
            @Override
            protected Task<List<FileTableModel>> createTask() {
                return new Task<List<FileTableModel>>() {
                    @Override
                    protected List<FileTableModel> call() {
                        List<FileTableModel> dataList = new ArrayList<>();
                        if (null != s3) {
                            ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
                            listObjectsV2Request.setBucketName(bucketName);
                            listObjectsV2Request.setDelimiter("/");
                            if (Utils.isNotNull(prefix)) {
                                listObjectsV2Request.setPrefix(prefix);
                            }
                            ListObjectsV2Result listObjectsV2Result = s3.listObjectsV2(listObjectsV2Request);

                            for (String commonPrefix : listObjectsV2Result.getCommonPrefixes()) {
                                dataList.add(new FileTableModel(commonPrefix, null, null, null, true));
                            }

                            for (S3ObjectSummary objectSummary : listObjectsV2Result.getObjectSummaries()) {
                                dataList.add(new FileTableModel(objectSummary.getKey(), objectSummary.getLastModified(), objectSummary.getStorageClass(), objectSummary.getSize(), false));
                            }

                        }
                        return dataList;
                    }
                };
            }
        };

        redisInfoService.setOnRunning(onRunningEvent -> {
            AppModel.setStatusText("获取数据中...");
            AppModel.setStatusDouble(-1);
        });
        redisInfoService.setOnFailed(onFailedEvent -> {
            AppModel.setStatusText("获取数据失败");
            AppModel.setStatusDouble(0);
            redisInfoService.getException().printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("获数据失败");
            alert.setHeaderText("获取数据失败\n" + redisInfoService.getException().getLocalizedMessage());
            alert.showAndWait();
        });

        redisInfoService.start();

        return redisInfoService;
    }


    public Service uploadFile(AmazonS3 s3, String bucketName, String prefixKey, File file) {

        Service uploadFileService = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        if (null != s3) {
                            s3.putObject(new PutObjectRequest(bucketName, prefixKey + file.getName(), file)
                                    .withCannedAcl(CannedAccessControlList.Private));
                        }
                        return null;
                    }

                };
            }
        };

        uploadFileService.setOnRunning(onRunningEvent -> {
            AppModel.setStatusText("文件上传中...");
            AppModel.setStatusDouble(-1);
        });
        uploadFileService.setOnFailed(onFailedEvent -> {
            AppModel.setStatusText("文件上传失败");
            AppModel.setStatusDouble(0);
            uploadFileService.getException().printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("文件上传失败");
            alert.setHeaderText("文件上传失败\n" + uploadFileService.getException().getLocalizedMessage());
            alert.showAndWait();
        });

        uploadFileService.start();
        return uploadFileService;

    }

    public Service downloadFile(AmazonS3 s3, String bucketName, String key, String targetFilePath) {

        Service downloadFileService = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        if (null != s3) {
                            S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
                            if (object != null) {
                                InputStream input = null;
                                FileOutputStream fileOutputStream = null;
                                byte[] data = null;
                                try {
                                    input = object.getObjectContent();
                                    data = new byte[input.available()];
                                    int len = 0;
                                    fileOutputStream = new FileOutputStream(targetFilePath);
                                    while ((len = input.read(data)) != -1) {
                                        fileOutputStream.write(data, 0, len);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    if (fileOutputStream != null) {
                                        try {
                                            fileOutputStream.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (input != null) {
                                        try {
                                            input.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                        }
                        return null;
                    }

                };
            }
        };

        downloadFileService.setOnRunning(onRunningEvent -> {
            AppModel.setStatusText("文件下载中...");
            AppModel.setStatusDouble(-1);
        });
        downloadFileService.setOnFailed(onFailedEvent -> {
            AppModel.setStatusText("文件下载失败");
            AppModel.setStatusDouble(0);
            downloadFileService.getException().printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("文件下载失败");
            alert.setHeaderText("文件下载失败\n" + downloadFileService.getException().getLocalizedMessage());
            alert.showAndWait();
        });

        downloadFileService.start();
        return downloadFileService;
    }

    public Service deleteFile(AmazonS3 s3, String bucketName, String key) {

        Service deleteFileService = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        if (null != s3) {
                            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, key);
                            s3.deleteObject(deleteObjectRequest);
                        }
                        return null;
                    }

                };
            }
        };

        deleteFileService.setOnRunning(onRunningEvent -> {
            AppModel.setStatusText("文件删除中...");
            AppModel.setStatusDouble(-1);
        });
        deleteFileService.setOnFailed(onFailedEvent -> {
            AppModel.setStatusText("文件删除失败");
            AppModel.setStatusDouble(0);
            deleteFileService.getException().printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("文件删除失败");
            alert.setHeaderText("文件删除失败\n" + deleteFileService.getException().getLocalizedMessage());
            alert.showAndWait();
        });

        deleteFileService.start();
        return deleteFileService;
    }

    public Service renameFile(AmazonS3 s3, String bucketName, String fromKey, String toKey) {

        Service deleteFileService = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        if (null != s3) {
                            CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucketName, fromKey, bucketName, toKey);
                            s3.copyObject(copyObjectRequest);
                            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, fromKey);
                            s3.deleteObject(deleteObjectRequest);
                        }
                        return null;
                    }

                };
            }
        };

        deleteFileService.setOnRunning(onRunningEvent -> {
            AppModel.setStatusText("文件重命名中...");
            AppModel.setStatusDouble(-1);
        });
        deleteFileService.setOnFailed(onFailedEvent -> {
            AppModel.setStatusText("文件重命名失败");
            AppModel.setStatusDouble(0);
            deleteFileService.getException().printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("文件重命名失败");
            alert.setHeaderText("文件重命名失败\n" + deleteFileService.getException().getLocalizedMessage());
            alert.showAndWait();
        });

        deleteFileService.start();
        return deleteFileService;
    }


}


