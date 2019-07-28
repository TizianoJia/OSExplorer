package net.easydebug.osexplorer.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.util.Date;

public class FileTableModel {

    private final ReadOnlyStringProperty fileName;
    private final ReadOnlyObjectProperty<Date> lastModified;
    private final ReadOnlyStringProperty storageClass;
    private final ReadOnlyLongProperty size;
    private final boolean directory;

    public FileTableModel(String fileName, Date lastModified, String storageClass, Long size, Boolean directory) {
        if (null != fileName) {
            this.fileName = new SimpleStringProperty(fileName);
        } else {
            this.fileName = new SimpleStringProperty();
        }
        if (null != lastModified) {
            this.lastModified = new SimpleObjectProperty<>(lastModified);
        }else{
            this.lastModified = new SimpleObjectProperty<>();
        }
        if (null != storageClass) {
            this.storageClass = new SimpleStringProperty(storageClass);
        }else{
            this.storageClass = new SimpleStringProperty();
        }
        if (null != size) {
            this.size = new SimpleLongProperty(size);
        }else{
            this.size = new SimpleLongProperty();
        }
        this.directory = directory;
    }

    public String getFileName() {
        return fileName.get();
    }

    public ReadOnlyStringProperty fileNameProperty() {
        return fileName;
    }

    public Date getLastModified() {
        return lastModified.get();
    }

    public ReadOnlyObjectProperty<Date> lastModifiedProperty() {
        return lastModified;
    }

    public String getStorageClass() {
        return storageClass.get();
    }

    public ReadOnlyStringProperty storageClassProperty() {
        return storageClass;
    }

    public long getSize() {
        return size.get();
    }

    public ReadOnlyLongProperty sizeProperty() {
        return size;
    }

    public boolean isDirectory() {
        return directory;
    }
}
