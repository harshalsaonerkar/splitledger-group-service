package com.splitledger.group.controller;

import com.splitledger.group.client.AuthServiceClient;
import com.splitledger.group.dto.AddMemberRequest;
import com.splitledger.group.dto.CreateGroupRequest;
import com.splitledger.group.dto.GroupResponse;
import com.splitledger.group.security.JwtTokenProvider;
import com.splitledger.group.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final AuthServiceClient authServiceClient;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            HttpServletRequest httpRequest) {

        String token = httpRequest.getHeader("Authorization").substring(7);
        UUID userId = jwtTokenProvider.extractUserId(token);
        String email = jwtTokenProvider.extractEmail(token);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(request, userId, email, email));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable UUID groupId,
            HttpServletRequest httpRequest) {

        String token = httpRequest.getHeader("Authorization").substring(7);
        UUID userId = jwtTokenProvider.extractUserId(token);
        return ResponseEntity.ok(groupService.getGroup(groupId, userId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GroupResponse>> getMyGroups(
            HttpServletRequest httpRequest) {

        String token = httpRequest.getHeader("Authorization").substring(7);
        UUID userId = jwtTokenProvider.extractUserId(token);
        return ResponseEntity.ok(groupService.getMyGroups(userId));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddMemberRequest request,
            HttpServletRequest httpRequest) {

        String token = httpRequest.getHeader("Authorization").substring(7);
        UUID requesterId = jwtTokenProvider.extractUserId(token);

        AuthServiceClient.UserInfo userInfo =
                authServiceClient.getUserByEmail(request.getEmail(),
                        "Bearer " + token);

        return ResponseEntity.ok(
                groupService.addMember(groupId, requesterId,
                        userInfo.getId(), userInfo.getEmail(), userInfo.getName()));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Map<String, String>> removeMember(@PathVariable UUID groupId, @PathVariable UUID userId, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        UUID requesterId = jwtTokenProvider.extractUserId(token);
        groupService.removeMember(groupId, requesterId, userId);
        return ResponseEntity.ok(Map.of("message", "Member removed"));
    }
}
