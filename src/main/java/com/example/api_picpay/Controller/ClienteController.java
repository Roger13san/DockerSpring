package com.example.api_picpay.Controller;

import com.example.api_picpay.Model.Cliente;
import com.example.api_picpay.Repository.ClienteRepository;
import com.example.api_picpay.Service.ClienteService;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.apache.coyote.Request;
import org.aspectj.apache.bcel.Repository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cliente")

public class ClienteController {
    private final Validator validator;
    private final ClienteService clienteService;

    //Objeto de criação do beans
    public ClienteController(ClienteService clienteService, Validator validator){
        this.clienteService = clienteService;
        this.validator = validator;
    }

    @GetMapping("/listarTudo")
    public List<Cliente> listarTudo(){
        return clienteService.findAll();
    }

    @GetMapping("/listarPeloId/{cpf}")
    public List<Cliente> listarPeloId(@PathVariable String cpf){
        List<Cliente> listaCliente = new ArrayList<>();
        listaCliente.add(clienteService.findById(cpf));
        return listaCliente;
    }

    @GetMapping("/listarPeloEmail")
    public List<Cliente> listarPeloEmail(@RequestParam String email){
        return clienteService.findByEmail(email);
    }
    @GetMapping("/listarPeloNome")
    public List<Cliente> listarPeloNome(@RequestParam String nome){
        return clienteService.findByName(nome);
    }
    @GetMapping("/listarPeloTelefone")
    public List<Cliente> listarPeloTelefone(@RequestParam String telefone){
        return clienteService.findByPhone(telefone);
    }

    @PostMapping("/inserir")
    public ResponseEntity<String> inserirCliente(@Valid @RequestBody Cliente cliente, BindingResult result){
        try{
            if(result.hasErrors()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(retornaErro(result));
            }else{
                clienteService.insertClient(cliente);
                return ResponseEntity.ok("Cliente foi inserido com sucesso");
            }
        }catch (Exception nnn){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno!");
        }
    }

    @PatchMapping("/atualizar/{id}")
    public ResponseEntity<String> atualizarCliente(@PathVariable String id, @RequestBody Map<String, Object> updates){
        try {
            Cliente clienteNovo = clienteService.findById(id);

            if (updates.containsKey("cpf")) {
                return ResponseEntity.ok("Não é possivel atualizar o CPF do cliente");
            }
            if (updates.containsKey("nome")) {
                final String nome = String.valueOf(updates.get("nome"));
                clienteNovo.setNome(nome);
            }
            if (updates.containsKey("email")) {
                final String email = String.valueOf(updates.get("email"));
                clienteNovo.setEmail(email);
            }
            if (updates.containsKey("telefone")) {
                final String telefone = String.valueOf(updates.get("telefone"));
                clienteNovo.setTelefone(telefone);
            }

            //Validar os dados
            DataBinder dataBinder = new DataBinder(clienteNovo);
            dataBinder.setValidator(validator);
            dataBinder.validate();
            BindingResult result = dataBinder.getBindingResult();
            if (result.hasErrors()) {
                return ResponseEntity.badRequest().body(retornaErro(result));
            }

            clienteService.updateClient(clienteNovo);
            return ResponseEntity.ok("Cliente atualizado com sucesso");
        } catch (RuntimeException npc) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente Não Encontrado!");
        }
    }

    @DeleteMapping("/deletar/{cpf}")
    @Transactional
    public ResponseEntity<String> excluirCliente(@PathVariable String cpf){
        try{
            clienteService.deleteClientById(cpf);
            return ResponseEntity.ok("Cliente foi excluido com sucesso");
        }catch (RuntimeException npc){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não foi excluido!");
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
