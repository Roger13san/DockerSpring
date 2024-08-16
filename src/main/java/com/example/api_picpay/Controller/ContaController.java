package com.example.api_picpay.Controller;

import com.example.api_picpay.Model.Conta;
import com.example.api_picpay.Service.ClienteService;
import com.example.api_picpay.Service.ContaService;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.naming.Binding;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conta")
public class ContaController {
    private final Validator validator;
    private final ContaService contaService;

    //Objeto de criação do bean
    public ContaController(ContaService contaService, Validator validator){
        this.contaService = contaService;
        this.validator = validator;
    }

    @GetMapping("/listarTudo")
    public List<Conta> listarTudo(){
        return contaService.findAll();
    }

    @GetMapping("/listarPeloSaldo")
    public List<Conta> listarPeloSaldoMaiorQue(@RequestParam Double saldo){
        return contaService.findAccountsBySaldo(saldo);
    }

    @GetMapping("/listarRangeLimiteEspecial")
    public List<Conta> listarRangeLimiteEspecial(@RequestBody Map<String, Double> valores){
        return contaService.findContasByLimiteEspecialBetween(valores.get("valorMinimo"), valores.get("valorMaximo"));
    }

    @GetMapping("/listarPeloCpf")
    public List<Conta> listaPeloCpf(@RequestParam String cpf){
        return contaService.findAccountByCpf(cpf);
    }

    @PostMapping("/inserirConta")
    public ResponseEntity<?> insertClient(@RequestBody Conta conta, BindingResult result){
        try{
            if(result.hasErrors()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(retornaErro(result));
            }else{
                if(contaService.insertAccount(conta) != null){
                    return ResponseEntity.ok("Conta inserida com sucesso");
                }else{
                    return ResponseEntity.ok("Conta já existente, caso queira atualizar o saldo, usar o endPoint /atualizarConta");
                }
            }
        }catch (Exception nnn){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno!");
        }
    }

    @PutMapping("/depositarSaldo")
    public ResponseEntity<?> atualizarSaldo(@RequestBody Map<String, Object> valores, BindingResult result){
        try{
            if(result.hasErrors()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(retornaErro(result));
            }else {
                if (valores.containsKey("numeroConta")) {
                    String numeroConta = String.valueOf(valores.get("numeroConta"));
                    double valorDeposito = Double.parseDouble(String.valueOf(valores.get("valorSaque")));

                    if (valorDeposito > 0) {
                        if (contaService.depositarValor(numeroConta, valorDeposito) != null) {
                            return ResponseEntity.ok("Valor depositado com sucesso!");
                        } else {
                            return ResponseEntity.ok("Não foi possivel fazer depositar o valor!");
                        }
                    } else {
                        return ResponseEntity.ok("Valor para ser depositado deve ser maior que 0!");
                    }
                } else {
                    return ResponseEntity.ok("Número da conta deve ser passado como parâmetro!");
                }
            }
        }catch (Exception nnn){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno!");
        }
    }

    @PutMapping("/sacarValor")
    public ResponseEntity<?> sacarValor(@RequestBody Map<String, Object> valores, BindingResult result){
        try{
            if(result.hasErrors()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(retornaErro(result));
            }else {
                if (valores.containsKey("numeroConta")) {
                    String numeroConta = String.valueOf(valores.get("numeroConta"));
                    double valorSaque = Double.parseDouble(String.valueOf(valores.get("valorSaque")));

                    if (validaFds(Calendar.getInstance())) {
                        if (valorSaque > 1000) {
                            return ResponseEntity.ok("Limite excedido! Nos finais de semana, o valor máximo de saque é R$1000 !");
                        } else {
                            if(valorSaque > 0) {
                                if (contaService.sacarValor(numeroConta, valorSaque) != null) {
                                    return ResponseEntity.ok("Valor foi sacado com sucesso!");
                                }else{
                                    return ResponseEntity.ok("Valor não foi sacado!");
                                }
                            }else{
                                return ResponseEntity.ok("Valor para o saque deve ser maior que 0!");
                            }
                        }
                    }else{
                        if(valorSaque > 0) {
                            if (contaService.sacarValor(numeroConta, valorSaque) != null) {
                                return ResponseEntity.ok("Valor foi sacado com sucesso!");
                            }else{
                                return ResponseEntity.ok("Valor não foi sacado!");
                            }
                        }else{
                            return ResponseEntity.ok("Valor para o saque deve ser maior que 0!");
                        }
                    }
                } else {
                    return ResponseEntity.ok("Número da conta deve ser passado como parâmetro!");
                }
            }
        }catch (Exception nnn){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno!");
        }
    }

    public boolean validaFds(Calendar data){
        if(data.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || data.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
            return true;
        }else{
            return false;
        }
    }

    @PutMapping("/transferirValor")
    public ResponseEntity<?> transferirValor(@RequestBody Map<String, Object> valores, BindingResult result){
        try {
            if (result.hasErrors()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(retornaErro(result));
            }else{
                if(valores.containsKey("numeroContaRetirar") && valores.containsKey("numeroContaInserir")){
                    String numeroContaRetirar = String.valueOf(valores.get("numeroContaRetirar"));
                    String numeroContaInserir = String.valueOf(valores.get("numeroContaInserir"));
                    String tipoTransferencia = String.valueOf(valores.get("tipoTransferencia"));
                    double valorTransferencia = Double.parseDouble(String.valueOf(valores.get("valorTransferencia")));

                    if(tipoTransferencia.equalsIgnoreCase("pix")){
                        if(!contaService.transferirValor(numeroContaRetirar, numeroContaInserir, valorTransferencia).isEmpty()) {
                            return ResponseEntity.ok("Transferência efetuada com sucesso!");
                        }else{
                            return ResponseEntity.ok("Transferência não foi efetuada, conta não encontrada!");
                        }
                    }else if(tipoTransferencia.equalsIgnoreCase("ted")){
                        if(validaHorarioComercial(Calendar.getInstance())){
                            if(!contaService.transferirValor(numeroContaRetirar, numeroContaInserir, valorTransferencia/*, tipoTransferencia*/).isEmpty()) {
                                return ResponseEntity.ok("Transferência efetuada com sucesso!");
                            }else{
                                return ResponseEntity.ok("Transferência não foi efetuada, conta não encontrada!");
                            }
                        }else{
                            return ResponseEntity.ok("Ted só poderá ser feito em horário comercial!");
                        }
                    }else{
                        return ResponseEntity.ok("Tipo de transferência inválido!");
                    }

                }else{
                    return ResponseEntity.ok("Número da conta deve ser passado como parâmetro!");
                }
            }
        }catch (Exception nnn){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno!");
        }
    }

    public boolean validaHorarioComercial(Calendar data){
        if((data.get(Calendar.DAY_OF_WEEK) >= 2 && data.get(Calendar.DAY_OF_WEEK) <= 6) && (data.get(Calendar.HOUR_OF_DAY) >= 8 && data.get(Calendar.HOUR_OF_DAY) <= 17)){
            return true;
        }else{
            return false;
        }
    }

    @DeleteMapping("/excluirConta/{numeroConta}")
    @Transactional
    public ResponseEntity<?> deletaConta(@PathVariable String numeroConta, BindingResult result){
        try{
            if(result.hasErrors()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(retornaErro(result));
            }else {
                contaService.deleteAccount(numeroConta);
                return ResponseEntity.ok("Conta foi excluida com sucesso!");
            }
        }catch (RuntimeException npc){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conta não foi excluida!");
        }
    }

    @PatchMapping("/alterarConta/{numConta}")
    public ResponseEntity<?> atualizaConta(@PathVariable String numConta, @RequestBody Map<String, Object> valoresAtualizar){
        try{
            Conta contaNova = contaService.findByAccountNumber(numConta);
            if(contaNova != null) {
                if (valoresAtualizar.containsKey("numeroConta")) {
                    return ResponseEntity.ok("Número da conta não pode ser alterado");
                }
                if (valoresAtualizar.containsKey("clienteCpf")) {
                    return ResponseEntity.ok("Cpf do cliente não pode ser 'alterado'");
                }
                if (valoresAtualizar.containsKey("saldo")) {
                    final double saldo = Double.parseDouble(String.valueOf(valoresAtualizar.get("saldo")));
                    contaNova.setSaldo(saldo);
                }
                if(valoresAtualizar.containsKey("limiteEspecial")){
                    final double limiteEspecial = Double.parseDouble(String.valueOf(valoresAtualizar.get("limiteEspecial")));
                    contaNova.setLimiteEspecial(limiteEspecial);
                }

                //Validar os dados
                DataBinder dataBinder = new DataBinder(contaNova);
                dataBinder.setValidator(validator);
                dataBinder.validate();
                BindingResult result = dataBinder.getBindingResult();
                if (result.hasErrors()) {
                    return ResponseEntity.badRequest().body(retornaErro(result));
                }

                contaService.updateAccount(contaNova);
                return ResponseEntity.ok("Alteração feita com sucesso!");
            }else{
                return ResponseEntity.ok("Não foi possivel encontrar a conta");
            }

        }catch (Exception npc){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conta não foi excluida!");
        }
    }

    public String retornaErro(BindingResult result) {
        StringBuilder stringBuilderErro = new StringBuilder();
        if (result.hasErrors()) {
            for (FieldError error : result.getFieldErrors()) {
                stringBuilderErro.append("Erro: ").append(error.getDefaultMessage()).append("  |  ");
            }
        }
        return stringBuilderErro.toString();
    }
}
