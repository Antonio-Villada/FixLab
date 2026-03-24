package com.software.fixlab.repository;

import com.software.fixlab.entity.ChatMensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMensajeRepository extends JpaRepository<ChatMensaje, Long> {

    List<ChatMensaje> findByUsuarioEmailOrderByCreadoEnAsc(String usuarioEmail);

    void deleteByUsuarioEmail(String usuarioEmail);
}
