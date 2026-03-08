package com.navi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.*;

@Getter
@Setter
public class AccountPerm {
    @NotBlank
    public String accountId;
    public Map<String,Object> permissions;
}
