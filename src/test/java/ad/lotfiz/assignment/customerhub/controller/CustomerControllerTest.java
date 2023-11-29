package ad.lotfiz.assignment.customerhub.controller;

import ad.lotfiz.assignment.customerhub.exception.CustomerNotFoundException;
import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import ad.lotfiz.assignment.customerhub.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.customerhub.api.v1.model.CustomerListResponse;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static ad.lotfiz.assignment.customerhub.RandomGenerator.randomCustomerRequest;
import static ad.lotfiz.assignment.customerhub.RandomGenerator.randomCustomerResponse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        CustomerResponse expectedResponse = getCustomerResponse(id);
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
        CustomerRequest request = new CustomerRequest("John", "Doe").age(25).address("123 Main St").email("john.doe@example.com");

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
    void testGetCustomer() throws Exception {
        // Given
        String customerId = "1";
        CustomerResponse expectedResponse = getCustomerResponse(customerId);
        when(customerService.fetchCustomer(customerId)).thenReturn(expectedResponse);

        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/customers/{customerId}", customerId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("John"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(25))
                .andExpect(MockMvcResultMatchers.jsonPath("$.address").value("123 Main St"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("john.doe@example.com"));

        // Then
        verify(customerService, times(1)).fetchCustomer(customerId);
    }

    @Test
    void testGetCustomer_customer_not_found() throws Exception {
        // Given
        String customerId = "1";

        // Mocking the behavior of CustomerService to throw a CustomerNotFoundException
        when(customerService.fetchCustomer(customerId))
                .thenThrow(new CustomerNotFoundException("Customer not found"));

        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/customers/{customerId}", customerId))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Customer not found"));

        // Then
        verify(customerService, times(1)).fetchCustomer(customerId);
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

    @Test
    void testListCustomers() throws Exception {
        // Given
        int page = 0;
        int size = 10;

        CustomerResponse customerResponse1 = randomCustomerResponse();
        CustomerResponse customerResponse2 = randomCustomerResponse();
        List<CustomerResponse> customerResponseList = Arrays.asList(customerResponse1, customerResponse2);

        when(customerService.list(any(Pageable.class))).thenReturn(new CustomerListResponse(page, size, customerResponseList));

        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/customers")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.page").value(page))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(size))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(customerResponse1.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].firstName").value(customerResponse1.getFirstName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].lastName").value(customerResponse1.getLastName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].age").value(customerResponse1.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].address").value(customerResponse1.getAddress()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].email").value(customerResponse1.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").value(customerResponse2.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].firstName").value(customerResponse2.getFirstName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].lastName").value(customerResponse2.getLastName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].age").value(customerResponse2.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].address").value(customerResponse2.getAddress()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].email").value(customerResponse2.getEmail()));

        // Then
        verify(customerService, times(1)).list(any(Pageable.class));
    }


    @Test
    void testUpdateCustomer() throws Exception {
        // Given
        String customerId = "1";
        CustomerRequest updatedCustomerRequest = randomCustomerRequest();
        CustomerResponse updatedCustomerResponse = randomCustomerResponse().id(customerId);
        when(customerService.update(eq(customerId), any(CustomerRequest.class))).thenReturn(updatedCustomerResponse);

        // When
        mockMvc.perform(MockMvcRequestBuilders.put("/customers/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCustomerRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(customerId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(updatedCustomerResponse.getFirstName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(updatedCustomerResponse.getLastName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(updatedCustomerResponse.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.address").value(updatedCustomerResponse.getAddress()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(updatedCustomerResponse.getEmail()));

        // Then
        verify(customerService, times(1)).update(eq(customerId), any(CustomerRequest.class));
    }

    @Test
    void testUpdateCustomer_customer_not_found() throws Exception {
        // Given
        String customerId = "1";
        CustomerRequest updatedCustomerRequest = randomCustomerRequest();

        // Mocking the behavior of CustomerService to throw a CustomerNotFoundException
        when(customerService.update(eq(customerId), any(CustomerRequest.class)))
                .thenThrow(new CustomerNotFoundException("Customer not found"));

        // When
        mockMvc.perform(MockMvcRequestBuilders.put("/customers/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCustomerRequest)))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Customer not found"));

        // Then
        verify(customerService, times(1)).update(eq(customerId), any(CustomerRequest.class));
    }

    private static CustomerResponse getCustomerResponse(String id) {
        return new CustomerResponse(id, OffsetDateTime.now(), "John", "Doe").age(25).address("123 Main St").email("john.doe@example.com");
    }


}
