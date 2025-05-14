package com.example.lotterysystem.dao.dataobject;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class BaseDO implements Serializable {
    private Long id;
    private Date gmtCreate;
    private Date gmtModified;
}
