package com.example.chatserver.chat.service;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatParticipant;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.chat.dto.ChatMessageReqDto;
import com.example.chatserver.chat.dto.ChatRoomListResDto;
import com.example.chatserver.chat.repository.ChatMessageRepository;
import com.example.chatserver.chat.repository.ChatParticipantRepository;
import com.example.chatserver.chat.repository.ChatRoomRepository;
import com.example.chatserver.chat.repository.ReadStatusRepository;
import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MemberRepository memberRepository;

    public ChatService(ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository, ChatMessageRepository chatMessageRepository, ReadStatusRepository readStatusRepository, MemberRepository memberRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.readStatusRepository = readStatusRepository;
        this.memberRepository = memberRepository;
    }

    public void saveMessage(Long roomId, ChatMessageReqDto req) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room cannot found"));

        // 보낸 사람 조회
        Member sender = memberRepository.findByEmail(req.getSenderEmail()).orElseThrow(() -> new EntityNotFoundException("member cannot found"));

        // 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(req.getMessage())
                .build();

        chatMessageRepository.save(chatMessage);

        // 사용자 별 읽음 여부 저장
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        for (ChatParticipant p : chatParticipants) {
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .member(p.getMember())
                    .chatMessage(chatMessage)
                    .isRead(p.getMember().equals(sender))
                    .build();
            readStatusRepository.save(readStatus);
        }
    }

    public void createGroupRoom(String roomName) {
        String email = getEmail();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(roomName)
                .isGroupChat("Y")
                .build();
        chatRoomRepository.save(chatRoom);
        // 채팅 참여자로 개설자를 추가
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<ChatRoomListResDto> getGroupChatRooms (){
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat("Y");
        List<ChatRoomListResDto> dtos = new ArrayList<>();
        for(ChatRoom r : chatRooms){
            ChatRoomListResDto dto = ChatRoomListResDto.builder()
                    .roomId(r.getId())
                    .roomName(r.getName())
                    .build();
            dtos.add(dto);
        }
        return dtos;
    }

    public void addParticipantToGroupChat(Long roomId){
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
        // member 조회
        String email = getEmail();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("member cannot be found"));
        // 이미 참여자인지 검증
        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member);
        if(participant.isEmpty()){
            // ChatParticipant 저장
            addParticipantToRoom(chatRoom, member);
        }
    }

    public void addParticipantToRoom(ChatRoom chatRoom, Member member){
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    private String getEmail(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new AccessDeniedException("로그인 필요");
        return authentication.getName();
    }
}
