package com.splitledger.group.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class GroupResponse {
    private UUID id;
    private String name;
    private String description;
    private String inviteCode;
    private UUID createdBy;
    private List<MemberDto> members;


    @Data
    @Builder
    public static class MemberDto {
        private UUID userId;
        private String userName;
        private String userEmail;
        private String role;
    }
}
