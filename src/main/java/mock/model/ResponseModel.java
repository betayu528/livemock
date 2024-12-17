package mock.model;

import java.util.Objects;

public class ResponseModel {
    private String errMsg = "ok";
    private Object data = null;
    private int errcode;

    public ResponseModel() {
    }

    public ResponseModel(String errMsg, int errcode) {
        this.errMsg = errMsg;
        this.errcode = errcode;
    }

    public ResponseModel(String errMsg, int errcode, Object data) {
        this.errMsg = errMsg;
        this.errcode = errcode;
        this.data = data;
    }

    public String getErrMsg() {
        return this.errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getErrcode() {
        return this.errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public ResponseModel errMsg(String errMsg) {
        setErrMsg(errMsg);
        return this;
    }

    public ResponseModel data(Object data) {
        setData(data);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ResponseModel)) {
            return false;
        }
        ResponseModel restResponseModel = (ResponseModel) o;
        return Objects.equals(errMsg, restResponseModel.errMsg) && Objects.equals(data, restResponseModel.data) && Objects.equals(errcode, restResponseModel.errcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errMsg, data, errcode);
    }

    @Override
    public String toString() {
        return "{" +
            " errMsg='" + getErrMsg() + "'" +
            ", data='" + getData() + "'" +
            ", errcode='" + getErrcode() + "'" +
            "}";
    }
}
