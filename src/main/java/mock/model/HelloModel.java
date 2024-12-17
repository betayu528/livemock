package mock.model;

import java.util.Objects;

public class HelloModel {
    private int count = 0;
    private long min = 0;
    private long max = 0;
    private long sum = 0;

    public HelloModel() {
    }

    public HelloModel(int count, long min, long max, long sum, int timeoutCnt, int rspInvalidCnt) {
        this.count = count;
        this.min = min;
        this.max = max;
        this.sum = sum;
        this.timeoutCnt = timeoutCnt;
        this.rspInvalidCnt = rspInvalidCnt;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getMin() {
        return this.min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMax() {
        return this.max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getSum() {
        return this.sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }

    public int getTimeoutCnt() {
        return this.timeoutCnt;
    }

    public void setTimeoutCnt(int timeoutCnt) {
        this.timeoutCnt = timeoutCnt;
    }

    public int getRspInvalidCnt() {
        return this.rspInvalidCnt;
    }

    public void setRspInvalidCnt(int rspInvalidCnt) {
        this.rspInvalidCnt = rspInvalidCnt;
    }

    public HelloModel count(int count) {
        setCount(count);
        return this;
    }

    public HelloModel min(long min) {
        setMin(min);
        return this;
    }

    public HelloModel max(long max) {
        setMax(max);
        return this;
    }

    public HelloModel sum(long sum) {
        setSum(sum);
        return this;
    }

    public HelloModel timeoutCnt(int timeoutCnt) {
        setTimeoutCnt(timeoutCnt);
        return this;
    }

    public HelloModel rspInvalidCnt(int rspInvalidCnt) {
        setRspInvalidCnt(rspInvalidCnt);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof HelloModel)) {
            return false;
        }
        HelloModel helloModel = (HelloModel) o;
        return count == helloModel.count && min == helloModel.min && max == helloModel.max && sum == helloModel.sum && timeoutCnt == helloModel.timeoutCnt && rspInvalidCnt == helloModel.rspInvalidCnt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, min, max, sum, timeoutCnt, rspInvalidCnt);
    }

    @Override
    public String toString() {
        return "{" +
            " count='" + getCount() + "'" +
            ", min='" + getMin() + "'" +
            ", max='" + getMax() + "'" +
            ", sum='" + getSum() + "'" +
            ", timeoutCnt='" + getTimeoutCnt() + "'" +
            ", rspInvalidCnt='" + getRspInvalidCnt() + "'" +
            "}";
    }
    private int timeoutCnt = 0;
    private int rspInvalidCnt = 0;
}
