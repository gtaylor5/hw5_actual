package taylor.gerard.hw5;

/**
 * Created by Gerard on 4/29/2017.
 */

public enum Status {

    Pending ("Pending"), Due("Due"), Done("Done");
    public String status;
    Status(String val) {
        status = val;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
