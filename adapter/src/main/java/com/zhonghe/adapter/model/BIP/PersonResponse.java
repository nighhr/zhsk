package com.zhonghe.adapter.model.BIP;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonResponse {
    private String code;
    private List<PersonData> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PersonData {
        private Group pk_group;
        private Integer enablestate;
        private Creator creator;
        private String code;
        private String mobile;
        private Integer dr;
        private Org pk_org;
        private Integer dataoriginflag;
        private String pk_psndoc;
        private List<Job> psnjobs;
        private String name;
        private String isshopassist;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date creationtime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date ts;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Group {
        private String code;
        private String name;
        private String pk;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Org {
        private String code;
        private String name;
        private String pk;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Creator {
        private String code;
        private String name;
        private String pk;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Job {
        private Group pk_group;
        private Psncl pk_psncl;
        private String psncode;
        private Integer dr;
        private Org pk_org;
        private Dept pk_dept;
        private String pk_psnjob;
        private Psndoc pk_psndoc;
        private String indutydate;
        private JobInfo pk_job;
        private Post pk_post;
        private String ismainjob;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date ts;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Psncl {
        private String code;
        private String name;
        private String pk;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Dept {
        private String code;
        private String name;
        private String pk;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Psndoc {
        private String code;
        private String name;
        private String pk;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JobInfo {
        private String code;
        private String name;
        private String pk;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Post {
        private String code;
        private String name;
        private String pk;
    }
}
