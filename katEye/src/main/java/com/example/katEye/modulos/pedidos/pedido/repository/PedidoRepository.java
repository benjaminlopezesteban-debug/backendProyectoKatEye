package com.example.katEye.modulos.pedidos.pedido.repository;

import com.example.katEye.modulos.pedidos.pedido.entity.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByNumero(String numero);

    Optional<Pedido> findByNumeroAndUsuarioId(String numero, Long usuarioId);

    Optional<Pedido> findByNumeroAndContactoCorreoIgnoreCaseAndUsuarioIdIsNull(
            String numero, String contactoCorreo);

    List<Pedido> findAllByOrderByIdDesc();

    List<Pedido> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    @Query("SELECT MAX(p.numero) FROM Pedido p WHERE p.numero LIKE CONCAT(:prefijo, '%')")
    Optional<String> ultimoNumeroConPrefijo(@Param("prefijo") String prefijo);

    @Query(value = "SELECT GET_LOCK(:prefijo, 10)", nativeQuery = true)
    Integer bloquearNumero(@Param("prefijo") String prefijo);

    @Query(value = "SELECT RELEASE_LOCK(:prefijo)", nativeQuery = true)
    Integer liberarNumero(@Param("prefijo") String prefijo);
}
