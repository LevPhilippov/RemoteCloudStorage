package com.filippov.HibernateUtils;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "files", schema = "cloudstoragedb", catalog = "")
public class FilesEntity {
    private String pathNameHash;
    private String fileName;
    private String path;
    private String children;
    private AuthDataEntity authdataById;

    @Id
    @Column(name = "path_name_hash", nullable = false, length = 128)
    public String getPathNameHash() {
        return pathNameHash;
    }

    public void setPathNameHash(String pathNameHash) {
        this.pathNameHash = pathNameHash;
    }

    @Basic
    @Column(name = "file_name", nullable = false, length = 128)
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Basic
    @Column(name = "path", nullable = true, length = 128)
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Basic
    @Column(name = "children", length = 128)
    public String getChildren() { return children;}

    public void setChildren(String children) { this.children = children;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilesEntity that = (FilesEntity) o;
        return Objects.equals(pathNameHash, that.pathNameHash) &&
                Objects.equals(fileName, that.fileName) &&
                Objects.equals(path, that.path) &&
                Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathNameHash, fileName, path, children);
    }

    @ManyToOne
    @JoinColumn(name = "id", referencedColumnName = "id", nullable = false)
    public AuthDataEntity getAuthdataById() {
        return authdataById;
    }

    public void setAuthdataById(AuthDataEntity authdataById) {
        this.authdataById = authdataById;
    }
}
