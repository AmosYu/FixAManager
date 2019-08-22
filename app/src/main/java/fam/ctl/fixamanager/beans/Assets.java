package fam.ctl.fixamanager.beans;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Assets{
    @Id(autoincrement = true)
    private long id;
    private String code;//编码
    private String company;//存放地点
    private String person;//处级管理员账号名
    private String address;//任务名
    private String kind;
    private int useless;//类别   1在库已核查   0在库未核查   2已核查未在库
    private String record;//预留二
    private String imageurl;
    private String others;//备注信息
    @Generated(hash = 606409694)
    public Assets(long id, String code, String company, String person,
            String address, String kind, int useless, String record,
            String imageurl, String others) {
        this.id = id;
        this.code = code;
        this.company = company;
        this.person = person;
        this.address = address;
        this.kind = kind;
        this.useless = useless;
        this.record = record;
        this.imageurl = imageurl;
        this.others = others;
    }
    @Generated(hash = 1373698660)
    public Assets() {
    }
    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getCode() {
        return this.code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getCompany() {
        return this.company;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public String getPerson() {
        return this.person;
    }
    public void setPerson(String person) {
        this.person = person;
    }
    public String getAddress() {
        return this.address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getKind() {
        return this.kind;
    }
    public void setKind(String kind) {
        this.kind = kind;
    }
    public int getUseless() {
        return this.useless;
    }
    public void setUseless(int useless) {
        this.useless = useless;
    }
    public String getRecord() {
        return this.record;
    }
    public void setRecord(String record) {
        this.record = record;
    }
    public String getImageurl() {
        return this.imageurl;
    }
    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
    public String getOthers() {
        return this.others;
    }
    public void setOthers(String others) {
        this.others = others;
    }
}
