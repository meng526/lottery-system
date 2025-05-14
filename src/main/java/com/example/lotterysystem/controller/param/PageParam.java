package com.example.lotterysystem.controller.param;

import lombok.Data;

@Data
public class PageParam {
    private Integer currentPage=1;
    private Integer pageSize=10;
    public Integer offset(){
        return (currentPage-1)*pageSize;
    }
}
