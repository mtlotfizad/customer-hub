package ad.lotfiz.assignment.customerhub.controller;

import ad.lotfiz.assignment.customerhub.RandomGenerator;
import ad.lotfiz.assignment.customerhub.exception.CustomerNotFoundException;
import ad.lotfiz.assignment.customerhub.exception.FieldNotFoundException;
import ad.lotfiz.assignment.customerhub.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.customerhub.api.v1.model.CustomerListResponse;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import nl.customerhub.api.v1.model.CustomerUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ad.lotfiz.assignment.customerhub.RandomGenerator.randomCustomerRequest;
import static ad.lotfiz.assignment.customerhub.RandomGenerator.randomCustomerUpdateRequest;
import static org.mockito.ArgumentMatchers.eq;
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
        CustomerRequest request = randomCustomerRequest();
        CustomerResponse expectedResponse = RandomGenerator.mapRequestToResponse(request);
        String id = expectedResponse.getId();
        when(customerService.createNewCustomer(any(CustomerRequest.class))).thenReturn(expectedResponse);

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.firstName").value(request.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(request.getLastName()))
                .andExpect(jsonPath("$.age").value(request.getAge()))
                .andExpect(jsonPath("$.address").value(request.getAddress()))
                .andExpect(jsonPath("$.email").value(request.getEmail()));

        // Then
        verify(customerService, times(1)).createNewCustomer(any(CustomerRequest.class));
    }

    @Test
    void testCreateNewCustomer_invalid_email_format() throws Exception {
        // Given
        CustomerRequest request = randomCustomerRequest().email("invalid-email");

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid request content."));
        // Then
        verify(customerService, times(0)).createNewCustomer(any(CustomerRequest.class));
    }

    @Test
    void testCreateNewCustomer_mandatory_fields() throws Exception {
        // Given
        CustomerRequest request = randomCustomerRequest().email("").address("");

        // Mocking the behavior of CustomerService to throw a DataIntegrityViolationException
        when(customerService.createNewCustomer(any(CustomerRequest.class)))
                .thenThrow(new FieldNotFoundException("Either Address or email should be provided"));

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Then
        verify(customerService, times(1)).createNewCustomer(any(CustomerRequest.class));

    }

    @Test
    void testCreateNewCustomer_invalid_large_age() throws Exception {
        // Given
        CustomerRequest request = randomCustomerRequest().age(3000);

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid request content."));
        // Then
        verify(customerService, times(0)).createNewCustomer(any(CustomerRequest.class));
    }

    @Test
    void testCreateNewCustomer_invalid_negative_age() throws Exception {
        // Given
        CustomerRequest request = randomCustomerRequest().age(-2);

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid request content."));
        // Then
        verify(customerService, times(0)).createNewCustomer(any(CustomerRequest.class));
    }


    @Test
    void testCreateNewCustomer_unique_constraint_fails() throws Exception {
        // Given
        CustomerRequest request = randomCustomerRequest();

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
        CustomerResponse expectedResponse = RandomGenerator.randomCustomerResponse();
        String customerId = expectedResponse.getId();
        when(customerService.fetchCustomer(customerId)).thenReturn(expectedResponse);

        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/customers/{customerId}", customerId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(customerId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(expectedResponse.getFirstName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(expectedResponse.getLastName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(expectedResponse.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.address").value(expectedResponse.getAddress()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(expectedResponse.getEmail()));

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

        CustomerResponse customerResponse1 = RandomGenerator.randomCustomerResponse();
        CustomerResponse customerResponse2 = RandomGenerator.randomCustomerResponse();
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
        CustomerUpdateRequest updatedCustomerRequest = randomCustomerUpdateRequest();
        CustomerResponse updatedCustomerResponse = RandomGenerator.randomCustomerResponse().id(customerId);
        when(customerService.update(eq(customerId), any(CustomerUpdateRequest.class))).thenReturn(updatedCustomerResponse);

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
        verify(customerService, times(1)).update(eq(customerId), any(CustomerUpdateRequest.class));
    }

    @Test
    void testUpdateCustomer_customer_not_found() throws Exception {
        // Given
        String customerId = "1";
        CustomerUpdateRequest updatedCustomerRequest = randomCustomerUpdateRequest();

        // Mocking the behavior of CustomerService to throw a CustomerNotFoundException
        when(customerService.update(eq(customerId), any(CustomerUpdateRequest.class)))
                .thenThrow(new CustomerNotFoundException("Customer not found"));

        // When
        mockMvc.perform(MockMvcRequestBuilders.put("/customers/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCustomerRequest)))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Customer not found"));

        // Then
        verify(customerService, times(1)).update(eq(customerId), any(CustomerUpdateRequest.class));
    }


    @Test
    void testFindCustomer() throws Exception {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        int page = 0;
        int size = 10;

        CustomerResponse customerResponse1 = RandomGenerator.randomCustomerResponse().firstName(firstName).lastName(lastName);
        CustomerResponse customerResponse2 = RandomGenerator.randomCustomerResponse().firstName(firstName + "1").lastName(lastName + "1");
        List<CustomerResponse> customerResponseList = Arrays.asList(customerResponse1, customerResponse2);
        when(customerService.findByName(eq(firstName), eq(lastName), any(Pageable.class))).thenReturn(new CustomerListResponse(page, size, customerResponseList));

        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/customers/find")
                        .param("firstName", firstName)
                        .param("lastName", lastName)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.page").value(page))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(size))
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
        verify(customerService, times(1)).findByName(eq(firstName), eq(lastName), any(Pageable.class));
    }

    @Test
    void testFindCustomer_empty_result() throws Exception {
        // Given
        String firstName = "Nonexistent";
        String lastName = "User";
        int page = 0;
        int size = 10;

        when(customerService.findByName(eq(firstName), eq(lastName), any(Pageable.class)))
                .thenReturn(new CustomerListResponse(page, size, Collections.emptyList()));

        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/customers/find")
                        .param("firstName", firstName)
                        .param("lastName", lastName)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.page").value(page))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(size))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isEmpty());

        // Then
        verify(customerService, times(1)).findByName(eq(firstName), eq(lastName), any(Pageable.class));
    }

}
