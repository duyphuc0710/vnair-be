package com.vnair.user.dto.Response.permission;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

import com.vnair.common.model.PageResponseAbstract;

@Getter
@Setter
public class PermissionPageReponse extends PageResponseAbstract {
    private List<PermissionResponse> permissions;
  
}
