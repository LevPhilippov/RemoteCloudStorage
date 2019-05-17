package com.filippov.HibernateUtils;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class FilesEntityPK implements Serializable {
    private int id;
    private String pathNameHash;

    @Column(name = "id", nullable = false, updatable = false)
    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "path_name_hash", nullable = false, length = 128, updatable = false)
    @Id
    public String getPathNameHash() {
        return pathNameHash;
    }

    public void setPathNameHash(String pathNameHash) {
        this.pathNameHash = pathNameHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilesEntityPK that = (FilesEntityPK) o;
        return id == that.id &&
                Objects.equals(pathNameHash, that.pathNameHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pathNameHash);
    }
}
