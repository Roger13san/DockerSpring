package com.example.api_picpay.Service;

import com.example.api_picpay.Model.Cliente;
import com.example.api_picpay.Repository.ClienteRepository;
import com.example.api_picpay.Repository.ContaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    public ClienteService(ClienteRepository clienteRepository){
        this.clienteRepository = clienteRepository;
    }

    //Criando o método para retornar tudo que está no banco
    public List<Cliente> findAll(){
        return clienteRepository.findAll();
    }

    //Fazendo um select pelo Cpf
    public Cliente findById(String cpf){
        return clienteRepository.findById(cpf).orElseThrow(() ->
                new RuntimeException("Cliente não encontrado"));
    }

    //Fazendo um select caso a pessoa tenha um valor de e-mail conforme passado como parâmetro
    public List<Cliente> findByEmail(String email){
        return clienteRepository.findClientesByEmailLikeIgnoreCase(email);
    }
    public List<Cliente> findByName(String name){
        return clienteRepository.findClientesByNomeLikeIgnoreCase(name);
    }

    public List<Cliente> findByPhone(String telefone){
        return clienteRepository.findClienteByTelefoneLikeIgnoreCase(telefone);
    }

    //Faz a inserção de um novo cliente
    public Cliente insertClient(Cliente cliente){
        return clienteRepository.save(cliente);
    }

    //Faz a atualização dos dados do cliente
    public Cliente updateClient(Cliente cliente){
        return clienteRepository.save(cliente);
    }

    //Excluindo o cliente pelo cpf
    public Cliente deleteClientById(String cpf){
        Cliente client = findById(cpf);
        clienteRepository.delete(client);
        return client;
    }

}
