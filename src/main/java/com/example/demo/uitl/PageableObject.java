package com.example.demo.uitl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageableObject<T> {

    private List<T> data;

    private Integer totalPage;

    private Integer currentPage;

    private Long totalElements;

    public PageableObject(Page<T> page){
        this.data = page.getContent();
        this.totalPage = page.getTotalPages();
        this.currentPage = page.getNumber();
        this.totalElements = page.getTotalElements();
    }
}