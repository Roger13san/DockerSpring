package com.example.api_picpay.Service;

import com.example.api_picpay.Model.Conta;
import com.example.api_picpay.Repository.ContaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

@Service
public class ContaService {
    private final ContaRepository contaRepository;
    public ContaService(ContaRepository contaRepository){
        this.contaRepository = contaRepository;
    }

    public List<Conta> findAll(){
        return contaRepository.findAll();
    }

    public Conta findByAccountNumber(String accountNumber){
        return contaRepository.findById(accountNumber).orElseThrow(() ->
                new RuntimeException("Conta não encontrado"));
    }

    public List<Conta> findAccountsBySaldo(double saldo){
        return contaRepository.findContasBySaldoGreaterThanEqual(saldo);
    }

    public List<Conta> findContasByLimiteEspecialBetween(double valorMinimo, double valorMaximo){
        return contaRepository.findContasByLimiteEspecialBetween(valorMinimo, valorMaximo);
    }

    public List<Conta> findAccountByCpf(String cpf){
        return contaRepository.findContasByClienteCpfEqualsIgnoreCase(cpf);
    }

    public Conta insertAccount(Conta conta){
        if(!verifContaExistente(conta.getClienteCpf())){
            Random random = new Random();
            int numeroAleatorio = random.nextInt(1000, 9999);
            int digito = 0;
            String [] valoresNumeroConta = String.valueOf(numeroAleatorio).split("");

            for(String num : valoresNumeroConta){
                digito += Integer.parseInt(num);
            }

            digito %= 10;
            String numeroConta = String.valueOf(numeroAleatorio) + digito;
            conta.setNumeroConta(numeroConta);

            return contaRepository.save(conta);
        }else{
            return null;
        }
    }

    public boolean verifContaExistente(String cpf){
        if(findAccountByCpf(cpf).isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    public Conta depositarValor(String numeroConta, double valorDeposito){
        Conta conta = findByAccountNumber(numeroConta);
        if(conta != null){
            conta.setSaldo(conta.getSaldo() + valorDeposito);
            return contaRepository.save(conta);
        }else{
            return null;
        }
    }

    public Conta sacarValor(String numeroConta, double valorSaque){
        Conta conta = findByAccountNumber(numeroConta);

        if(verificaSaldoConta(conta, valorSaque)){
            conta.setSaldo(conta.getSaldo() - valorSaque);
            return contaRepository.save(conta);
        }else{
            return null;
        }
    }

    public List<Conta> transferirValor(String numeroContaRetirar, String numeroContaInserir, double valorTransferencia /*, String tipoTransferencia*/){
        Conta contaRetirada = findByAccountNumber(numeroContaRetirar);
        Conta contaInserir = findByAccountNumber(numeroContaInserir);

        if ((verifContaExistente(contaRetirada.getClienteCpf()) && verifContaExistente(contaInserir.getClienteCpf())) || verifContaExistente(contaRetirada.getClienteCpf()) || verifContaExistente(contaInserir.getClienteCpf())) {
            return validaTransferenciaValor(contaRetirada, contaInserir, valorTransferencia);
        } else {
            return null;
        }
    }

    public List<Conta> validaTransferenciaValor(Conta contaRetirada, Conta contaInserir, double valorTransferencia){
        if (verificaSaldoConta(contaRetirada, valorTransferencia)) {
            contaRetirada.setSaldo(contaRetirada.getSaldo() - valorTransferencia);
            contaInserir.setSaldo(contaInserir.getSaldo() + valorTransferencia);

            List<Conta> contas = new ArrayList<>();
            contas.add(contaInserir);
            contas.add(contaRetirada);

            return contaRepository.saveAll(contas);
        } else {
            return null;
        }
    }

    public boolean verificaSaldoConta(Conta contaRetirada, double valorSaque){
        if(contaRetirada.getSaldo() >= valorSaque){
            return true;
        }else{
            return false;
        }
    }

    public Conta updateAccount(Conta updatedAccount){
        return contaRepository.save(updatedAccount);
    }

    public Conta deleteAccount(String accountNumber){
        Conta account = findByAccountNumber(accountNumber);
        contaRepository.delete(account);
        return account;
    }
}
