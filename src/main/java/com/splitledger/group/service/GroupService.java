package com.splitledger.group.service;

import com.splitledger.group.dto.CreateGroupRequest;
import com.splitledger.group.dto.GroupResponse;
import com.splitledger.group.entity.Group;
import com.splitledger.group.entity.GroupMember;
import com.splitledger.group.enums.MemberRole;
import com.splitledger.group.repository.GroupMemberRepository;
import com.splitledger.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request,
                                     UUID creatorId,
                                     String creatorEmail,
                                     String creatorName) {
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(creatorId)
                .inviteCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();

        groupRepository.save(group);

        // creator is automatically ADMIN member
        GroupMember creator = GroupMember.builder()
                .group(group)
                .userId(creatorId)
                .userEmail(creatorEmail)
                .userName(creatorName)
                .role(MemberRole.ADMIN)
                .build();

        groupMemberRepository.save(creator);

        return toResponse(group, List.of(creator));
    }

    public GroupResponse getGroup(UUID groupId, UUID requesterId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        validateMember(groupId, requesterId);

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return toResponse(group, members);
    }

    public List<GroupResponse> getMyGroups(UUID userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
        return memberships.stream().map(m -> {
            Group group = m.getGroup();
            List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());
            return toResponse(group, members);
        }).collect(Collectors.toList());
    }

    @Transactional
    public GroupResponse addMember(UUID groupId,
                                   UUID requesterId,
                                   UUID newUserId,
                                   String newUserEmail,
                                   String newUserName) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        validateAdmin(groupId, requesterId);

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, newUserId)) {
            throw new RuntimeException("User is already a member of this group");
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .userId(newUserId)
                .userEmail(newUserEmail)
                .userName(newUserName)
                .role(MemberRole.MEMBER)
                .build();

        groupMemberRepository.save(member);

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return toResponse(group, members);
    }

    @Transactional
    public void removeMember(UUID groupId, UUID requesterId, UUID targetUserId) {
        validateAdmin(groupId, requesterId);

        GroupMember member = groupMemberRepository
                .findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        groupMemberRepository.delete(member);
    }

    private void validateMember(UUID groupId, UUID userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new RuntimeException("Access denied — not a group member");
        }
    }

    private void validateAdmin(UUID groupId, UUID userId) {
        GroupMember member = groupMemberRepository
                .findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));
        if (member.getRole() != MemberRole.ADMIN) {
            throw new RuntimeException("Access denied — admin only");
        }
    }

    private GroupResponse toResponse(Group group, List<GroupMember> members) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .inviteCode(group.getInviteCode())
                .createdBy(group.getCreatedBy())
                .members(members.stream().map(m -> GroupResponse.MemberDto.builder()
                        .userId(m.getUserId())
                        .userName(m.getUserName())
                        .userEmail(m.getUserEmail())
                        .role(m.getRole().name())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
