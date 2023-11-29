package ad.lotfiz.assignment.customerhub.controller;

import ad.lotfiz.assignment.customerhub.exception.CustomerNotFoundException;
import ad.lotfiz.assignment.customerhub.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(CustomerController.class)
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    void testCreateNewCustomer() throws Exception {
        // Given
        CustomerRequest request = new CustomerRequest("John", "Doe").age(25).address("123 Main St").email("john.doe@example.com");
        String id = UUID.randomUUID().toString();
        CustomerResponse expectedResponse = new CustomerResponse(id, OffsetDateTime.now(), "John", "Doe").age(25).address("123 Main St").email("john.doe@example.com");
        when(customerService.createNewCustomer(any(CustomerRequest.class))).thenReturn(expectedResponse);

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        // Then
        verify(customerService, times(1)).createNewCustomer(any(CustomerRequest.class));
    }

    @Test
    void testCreateNewCustomer_unique_constraint_fails() throws Exception {
        // Given
        CustomerRequest request =  new CustomerRequest("John", "Doe").age(25).address("123 Main St").email("john.doe@example.com");

        // Mocking the behavior of CustomerService to throw a DataIntegrityViolationException
        when(customerService.createNewCustomer(any(CustomerRequest.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());

        // Then
        verify(customerService, times(1)).createNewCustomer(any(CustomerRequest.class));
    }

    @Test
    void testDeleteCustomer() throws Exception {
        // Given
        String customerId = "1";

        // When
        mockMvc.perform(MockMvcRequestBuilders.delete("/customers/{customerId}", customerId))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // Then
        verify(customerService, times(1)).delete(customerId);
    }

    @Test
    void testDeleteCustomer_customer_not_found() throws Exception {
        // Given
        String customerId = "1";

        // Mocking the behavior of CustomerService to throw a CustomerNotFoundException
        doThrow(new CustomerNotFoundException("Customer not found"))
                .when(customerService).delete(customerId);

        // When
        mockMvc.perform(MockMvcRequestBuilders.delete("/customers/{customerId}", customerId))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Customer not found"));

        // Then
        verify(customerService, times(1)).delete(customerId);
    }


    @Test
    void testHandleIllegalArgumentException() throws Exception {
        // Given
        String invalidUuid = "not_a_valid_uuid";

        // Mocking the behavior of CustomerService to throw an IllegalArgumentException
        when(customerService.fetchCustomer(invalidUuid))
                .thenThrow(new IllegalArgumentException("Invalid UUID string: not_a_valid_uuid"));

        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/customers/{customerId}", invalidUuid))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid UUID string: not_a_valid_uuid"));

    }

}
