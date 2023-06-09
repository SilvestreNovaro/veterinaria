package com.example.veterinaria.service;

import com.example.veterinaria.DTO.CustomerDTO;
import com.example.veterinaria.entity.Customer;
import com.example.veterinaria.entity.Pet;
import com.example.veterinaria.entity.Role;
import com.example.veterinaria.exception.NotFoundException;
import com.example.veterinaria.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    private PetService petService;

    private RoleService roleService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();



    public Customer createCustomer(Customer customer) {

        Customer customer1 = new Customer();
        customer1.setName(customer.getName());
        customer1.setLastName(customer.getLastName());
        customer1.setAddress(customer.getAddress());
        customer1.setEmail(customer.getEmail());
        customer1.setContactNumber(customer.getContactNumber());
        customer1.setPets(customer.getPets());
        customer1.setRole(customer.getRole());

        String encodedPassword = this.passwordEncoder.encode(customer.getPassword());
        customer1.setPassword(encodedPassword);

        return customerRepository.save(customer1);
    }

    public Customer createCustomerDTO(CustomerDTO customerDTO) {

        Customer customer1 = new Customer();
        customer1.setName(customerDTO.getName());
        customer1.setLastName(customerDTO.getLastName());
        customer1.setAddress(customerDTO.getAddress());
        customer1.setEmail(customerDTO.getEmail());
        customer1.setContactNumber(customerDTO.getContactNumber());
        List<Pet> pets = new ArrayList<>();
        for (Long petId : customerDTO.getPet_ids()) {
            pets.add(petService.getPetById(petId).orElseThrow(() -> new RuntimeException("Pet not found with ID: " + petId)));
        }
        customer1.setPets(pets);
        customer1.setRole(roleService.findById(customerDTO.getRole_id()).get());


        String encodedPassword = this.passwordEncoder.encode(customerDTO.getPassword());
        customer1.setPassword(encodedPassword);

        return customerRepository.save(customer1);
    }



    public void updateCustomer(Customer customer, Long id) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if(optionalCustomer.isPresent()){
            // como no puedo setearle a un optional, guardo en una variable ya del tipo del objeto Customer para setearle los valores.
            Customer existingCustomer = optionalCustomer.get();
            if(customer.getName() !=null && !customer.getName().isEmpty()) existingCustomer.setName(customer.getName());
            if(customer.getLastName() !=null && !customer.getLastName().isEmpty()) existingCustomer.setLastName(customer.getLastName());
            if(customer.getAddress() !=null && !customer.getAddress().isEmpty()) existingCustomer.setAddress(customer.getAddress());
            if(customer.getContactNumber() !=null && !customer.getContactNumber().equals("")) existingCustomer.setContactNumber(customer.getContactNumber());
            if(customer.getEmail() !=null && !customer.getEmail().isEmpty()) existingCustomer.setEmail(customer.getEmail());
            if(customer.getPets() !=null && !customer.getPets().isEmpty()) existingCustomer.setPets(customer.getPets());
            if(customer.getRole() !=null && !customer.getRole().equals("")) existingCustomer.setRole(customer.getRole());
            if(customer.getPassword() !=null && !customer.getPassword().isEmpty()){
                String encodedPassword = this.passwordEncoder.encode(customer.getPassword());
                existingCustomer.setPassword(encodedPassword);
            }

            customerRepository.save(existingCustomer);
        }

    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    public Optional<Customer> findByName(String name){
        return customerRepository.findByName(name);
    }

    // add any additional methods here

    public List<Pet> findCustomersPets(Long Petid) {
        List<Pet> petList = new ArrayList<>();
         List<Customer> customerList = customerRepository.findAll();
        for (Customer customer : customerList) {
            var petList1 = customer.getPets();
            for (Pet pet : petList) {
                if (pet.getId().equals(Petid))
                   petList.add(pet);
            }
        }
        return petList;
    }

    public Optional<Customer> findByEmail(String email){
        return customerRepository.findByEmail(email);
    }

    public Optional<Customer> findById(Long id){
        return customerRepository.findById(id);
    }





    public List<Pet> findPetsByCustomerName(String name) {
        return customerRepository.findPetsByCustomerName(name);
    }

    public List<Customer> findCustomersByPetName(String petName) {
        return customerRepository.findCustomersByPetName(petName);
    }

    // otros métodos del servicio de Customer

    public void addPetToCustomer(Long customerId, Pet pet) {
        Customer customer = customerRepository.findById(customerId).get();
        customer.getPets().add(pet);
        customerRepository.save(customer);
    }

    public void addPetToCustomer(Long customerId, Long petId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
        Pet pet = petService.getPetById(petId).orElseThrow(() -> new RuntimeException("Pet not found"));
        Pet newPet = new Pet();
        newPet.setId(pet.getId());
        newPet.setPetName(pet.getPetName());
        newPet.setMedicalHistory(pet.getMedicalHistory());
        newPet.setAge(pet.getAge());
        newPet.setGender(pet.getGender());
        newPet.setPetSpecies(pet.getPetSpecies());
        customer.getPets().add(newPet);
        customerRepository.save(customer);
    }

    public void addRoleToCustomer(Long customerId, Role role){
        Customer customer = customerRepository.findById(customerId).get();
        //if(customer.getRole() == null || customer.getRole().equals(""))
        customer.setRole(role);
        customerRepository.save(customer);
    }

    public void addAnimalToCustomer(Long customerId, Pet pet){
        Customer customer = customerRepository.findById(customerId).get();
        customer.getPets().add(pet);
        customerRepository.save(customer);
    }




        public void deletePetById(Long customerId, Long petId) {
            Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
            if (optionalCustomer.isPresent()) {
                Customer customer = optionalCustomer.get();
                List<Pet> pets = customer.getPets();
                Optional<Pet> optionalPet = pets.stream().filter(p -> p.getId().equals(petId)).findFirst();
                if (optionalPet.isPresent()) {
                    Pet pet = optionalPet.get();
                    pets.remove(pet);
                    customer.setPets(pets);
                    customerRepository.save(customer);
                } else {
                    throw new NotFoundException("Pet not found with id: " + petId);
                }
            } else {
                throw new NotFoundException("Customer not found with id: " + customerId);
            }
        }

    public List<Customer> findCustomerByRoleId(Long id){
        return customerRepository.findCustomerByRoleId(id);
    }
        // otros métodos del servicio de Customer

    public void deleteRoleById(Long customerId, Long roleId){
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if(optionalCustomer.isPresent()){
            Customer customer = optionalCustomer.get();
            if(customer.getRole() !=null && customer.getRole().getId().equals(roleId)){
                customer.setRole(null);
                customerRepository.save(customer);
                //roleService.delete(roleId); Si agrego esta linea, borra el rol del customer y el rol de la base de datos.
            }else {
                throw new NotFoundException("Role not found with the id " + roleId);
            }
            }else {
                throw new NotFoundException("Customer not found with the id " + customerId);
            }


        }


    }







