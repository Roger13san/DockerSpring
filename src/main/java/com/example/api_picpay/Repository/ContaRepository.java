package com.example.api_picpay.Repository;

import com.example.api_picpay.Model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContaRepository extends JpaRepository<Conta, String> {
    List<Conta> findContasBySaldoGreaterThanEqual(double saldo);
    List<Conta> findContasByLimiteEspecialBetween(double limiteMinimo, double limiteMaximo);

    List<Conta> findContasByClienteCpfEqualsIgnoreCase(String cpf);

}

