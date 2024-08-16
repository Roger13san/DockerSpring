package com.example.api_picpay.Repository;

import com.example.api_picpay.Model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, String> {

    List<Cliente> findClientesByEmailLikeIgnoreCase(String valorGmail);
    List<Cliente> findClientesByNomeLikeIgnoreCase(String sobrenome);
    List<Cliente> findClienteByTelefoneLikeIgnoreCase(String valor);

}
