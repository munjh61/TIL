package com.example.chatserver.chat.repository;

import com.example.chatserver.chat.domain.ReadState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStateRepository extends JpaRepository<ReadState, Long> {
}
