package com.vnair.user.dto.Response.role;

import java.util.List;

import com.vnair.common.model.PageResponseAbstract;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePageResponse  extends PageResponseAbstract{
    List<RoleResponse> roles;
    
}
