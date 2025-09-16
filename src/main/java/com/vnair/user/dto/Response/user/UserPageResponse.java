package com.vnair.user.dto.Response.user;

import lombok.*;
import java.util.List;

import com.vnair.common.model.PageResponseAbstract;

@Getter
@Setter
@RequiredArgsConstructor
public class UserPageResponse extends PageResponseAbstract {
    private List<UserResponse> users;
}
