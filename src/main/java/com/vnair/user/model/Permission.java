package com.vnair.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;


import com.vnair.common.model.AbstractEntity;


@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_permission")
public class Permission extends AbstractEntity<Integer> {

    private String resource;  // USER, ORDER
    private String action;    // READ, CREATE
    private String scope;     // SELF, ALL

    public String getAuthority() {
        return resource + ":" + action + ":" + scope;
    }
}
