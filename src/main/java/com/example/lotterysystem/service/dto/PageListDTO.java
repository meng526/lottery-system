package com.example.lotterysystem.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageListDTO<T> implements Serializable {
    private Integer total;

    private List<T> records;

    public PageListDTO(Integer total,List<T> records){
        this.total=total;
        this.records=records;
    }



}
