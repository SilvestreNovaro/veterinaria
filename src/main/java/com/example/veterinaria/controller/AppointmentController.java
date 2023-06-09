package com.example.veterinaria.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.example.veterinaria.DTO.AppointmentDTO;
import com.example.veterinaria.entity.Appointment;
import com.example.veterinaria.entity.Customer;
import com.example.veterinaria.entity.Pet;
import com.example.veterinaria.entity.Vet;
import com.example.veterinaria.service.AppointmentService;
import com.example.veterinaria.service.CustomerService;
import com.example.veterinaria.service.PetService;
import com.example.veterinaria.service.VetService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Validated
@RestControllerAdvice
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private final AppointmentService appointmentService;

    private final CustomerService customerService;

    private final VetService vetService;

    private final PetService petService;
    private JavaMailSender javaMailSender;


    @GetMapping("/list")
    public List<Appointment> list(){
        return appointmentService.getAllAppointments();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentDTO appointmentDTO) throws MessagingException {

        Long customerId = appointmentDTO.getCustomer_id();
        Long vetId = appointmentDTO.getVet_id();
        LocalDateTime localDateTime = appointmentDTO.getAppointmentDateTime();
        /*List<Long> petsIds = appointmentDTO.getPets_ids();

        if(petsIds.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("petsIds " + petsIds + " not found");
        }

         */
        Optional<Customer> optionalCustomer = customerService.getCustomerById(customerId);
        Optional<Vet> optionalVetId = vetService.getVetById(vetId);
        Optional<Appointment> appointmentOptional = appointmentService.findByAppointmentDateTime(localDateTime);


        if(optionalCustomer.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("customerId " + customerId + " not found");
        }
        if(optionalVetId.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("vetId " + vetId + " not found");
        }
        if(appointmentOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("An appointment is already created by the exact same time "+ localDateTime);
        }
        if(localDateTime == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Local date time cant be null");
        }




        Appointment savedAppointment = appointmentService.createAppointment(appointmentDTO);


        // Crear el mensaje de correo electrónico
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(optionalCustomer.get().getEmail());
        helper.setSubject("¡Appointment confirmation!");
        String htmlMsg =
                "<html>" +
                        "<head>" +
                        "<style>" +
                        "table {" +
                        "  border-collapse: collapse;" +
                        "  width: 100%;" +
                        "}" +
                        "th, td {" +
                        "  text-align: left;" +
                        "  padding: 8px;" +
                        "}" +
                        "th {" +
                        "  background-color: #dddddd;" +
                        "  color: #333333;" +
                        "}" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<h1 style='color: #007bff;'>Confirmación de reserva</h1>" +
                        "<p>Estimado/a " + optionalCustomer.get().getName()+ ",</p>" +
                        "<p>Please, review the details of your reservation in the following table:</p>" +
                        "<table>" +
                        "<tr>" +
                        "<th>Customer</th>" +
                        "<th>appointmentReason</th>" +
                        "<th>appointmentNotes</th>" +
                        "<th>Vet</th>" +
                        "<th>Pet</th>" +
                        "</tr>" +
                        "<tr>" +
                        "<td>" + optionalCustomer.get().getName() + "</td>" +
                        "<td>" + appointmentDTO.getAppointmentReason().toString() + "</td>" +
                        "<td>" + appointmentDTO.getAppointmentNotes().toString() + "</td>" +
                        "<td>" + optionalVetId.get().getName()+ "</td>" +
                        "td>" + optionalCustomer.get().getPets().toString()+ "</td>" +
                        "<td>" + formattedDateTime+ "</td>" +
                        "</tr>" +
                        "</table>" +
                        "<p>Hope to see you soon!.</p>" +
                        "<p>Sincirely,</p>" +
                        "<p>The vet</p>" +
                        "</body>" +
                        "</html>";
        helper.setText(htmlMsg, true);
        javaMailSender.send(message);


        return new ResponseEntity<>(savedAppointment, HttpStatus.CREATED);
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<?> update(@RequestBody AppointmentDTO appointmentDTO, @PathVariable Long id){
        Optional<Appointment> optionalAppointment = appointmentService.getAppointmentById(id);
        if(optionalAppointment.isPresent()){
            appointmentService.updateAppointment(appointmentDTO, id);
            return ResponseEntity.status(HttpStatus.CREATED).body("Appointment updated succesfully!");

        }


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No appointment for the id " + id);

    }


    @GetMapping("/findById/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        Optional<Appointment> appointment = appointmentService.getAppointmentById(id);
        if (appointment.isPresent()) {
            return ResponseEntity.ok(appointment);
        } else {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("no appointment found for the id " + id);
        }
    }

    @GetMapping("/findAllAppointments")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getAppointmentsByCustomerId(@PathVariable Long customerId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByCustomerId(customerId);
        if(appointments.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("no appointment found for the Customer id " + customerId);
        }
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping("/vet/{vetId}")
    public ResponseEntity<?> getAppointmentsByVetId(@PathVariable Long vetId){
        List<Appointment> appointmentList = appointmentService.findByVetId(vetId);
        if(appointmentList.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("no appointment found for the Vet id " + vetId);
        }
        return new ResponseEntity<>(appointmentList, HttpStatus.OK);
    }


    //FOUND VET BY LICENSE.
    @GetMapping("/vetLicense/{vetLicense}")
    public ResponseEntity<?> getAppointmentsByLicense(@PathVariable String vetLicense){
        List<Appointment> appointmentList = appointmentService.findByLicense(vetLicense);
        if(appointmentList.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("no appointment found for the Vet license " + vetLicense);
        }
        return new ResponseEntity<>(appointmentList, HttpStatus.OK);
    }



    //DELTE 1 APPOINTMENT BY ID.

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointmentId(id);
        return ResponseEntity.ok("Appointment with id " + id + " deleted");
    }


    //DELETE MANY APPOINTMENTS.
    @DeleteMapping("/deleteByIds")
    public ResponseEntity<Object> deleteAppointmentsByIds(@RequestParam List <Long> appointmentIds) {
        var deletedIds = appointmentService.deleteAppointment(appointmentIds);
        if(deletedIds.size()>0){
            return ResponseEntity.ok("non existent ids " + deletedIds);
        }else{
            return ResponseEntity.ok("appointents deleted succesfully " + appointmentIds.toString());
        }

    }

    /*
    @DeleteMapping("/deleteAppointmentByIds2")
    public ResponseEntity<?> deleteAppointmentsByIds(@RequestParam Long[] appointmentIds) {
        Long[] deletedIds = appointmentService.deleteAppointmentsByIds(appointmentIds);
        if (deletedIds.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontraron citas con los IDs proporcionados.");
        } else {
            return ResponseEntity.ok("Se eliminaron las citas con los siguientes IDs: " + Arrays.toString(deletedIds));
        }
    }

     */

    // ALSO DELETES MANY APPOINTEMTS
    @DeleteMapping("/deleteAppointmentByIds2")
    public ResponseEntity<?> deleteAppointmentsByIds(@RequestParam  Long[] appointmentIds) {
        return appointmentService.deleteAppointmentsByIds(appointmentIds);
    }



}

