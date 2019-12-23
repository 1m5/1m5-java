package io.onemfive.data;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class Contract implements Persistable {

    public enum Status {NEW,ACTIVE,RETIRED}

    private Status status = Status.NEW;

    private Long id;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
