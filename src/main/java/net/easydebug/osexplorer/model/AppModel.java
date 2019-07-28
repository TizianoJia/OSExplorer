package net.easydebug.osexplorer.model;

import javafx.beans.property.*;
import javafx.collections.ObservableList;

import static javafx.collections.FXCollections.observableArrayList;

public class AppModel {

    private static StringProperty statusText = new SimpleStringProperty();

    private static SimpleDoubleProperty statusDouble = new SimpleDoubleProperty();

    private static StringProperty pathText = new SimpleStringProperty();

    public static String getStatusText() {
        return statusText.get();
    }

    public static StringProperty statusTextProperty() {
        return statusText;
    }

    private static ListProperty<FileTableModel> fileTableModelList = new SimpleListProperty<>(observableArrayList());

    public static void setStatusText(String statusText) {
        AppModel.statusText.set(statusText);
    }

    public static double getStatusDouble() {
        return statusDouble.get();
    }

    public static SimpleDoubleProperty statusDoubleProperty() {
        return statusDouble;
    }

    public static void setStatusDouble(double statusDouble) {
        AppModel.statusDouble.set(statusDouble);
    }

    public static String getPathText() {
        return pathText.get();
    }

    public static StringProperty pathTextProperty() {
        return pathText;
    }

    public static void setPathText(String pathText) {
        AppModel.pathText.set(pathText);
    }

    public static ObservableList<FileTableModel> getFileTableModelList() {
        return fileTableModelList.get();
    }

    public static ListProperty<FileTableModel> fileTableModelListProperty() {
        return fileTableModelList;
    }

    public static void setFileTableModelList(ObservableList<FileTableModel> fileTableModelList) {
        AppModel.fileTableModelList.set(fileTableModelList);
    }
}
