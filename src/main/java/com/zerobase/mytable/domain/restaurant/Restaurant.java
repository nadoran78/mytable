package com.zerobase.mytable.domain.restaurant;

import com.zerobase.mytable.domain.Store;
import com.zerobase.mytable.type.Table;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import java.util.List;

@Entity
public class Restaurant extends Store {
    @ElementCollection(targetClass = Table.class, fetch = FetchType.LAZY)
    private List<Table> tables;

    // from 메소드 사용할 경우 테이블 정렬해서 입력
    // request.getTables().sort(Comparator.comparingInt(Table::getTableVolume));
}
