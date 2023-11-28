package ad.lotfiz.assignment.customerhub.service;

import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import ad.lotfiz.assignment.customerhub.repository.CustomerRepository;
import ad.lotfiz.assignment.customerhub.service.mapper.CustomerMapper;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CustomerServiceTest {

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void testCreateNewCustomer() {
        // Given
        CustomerRequest customerRequest = new CustomerRequest("John", "Doe").age(25).address("123 Main St").email("john.doe@example.com");
        UUID id = UUID.randomUUID();
        CustomerEntity mockedEntity = new CustomerEntity(id, "John", "Doe", 25, "123 Main St", "john.doe@example.com");
        CustomerResponse customerResponse = new CustomerResponse(id.toString(), OffsetDateTime.now(), "John", "Doe").age(25).address("123 Main St").email("john.doe@example.com");
        when(customerMapper.mapFromCustomerRequest(customerRequest)).thenReturn(mockedEntity);
        when(customerMapper.mapFromCustomerEntity(mockedEntity)).thenReturn(customerResponse);
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(mockedEntity);

        // When
        CustomerResponse result = customerService.createNewCustomer(customerRequest);

        // Then
        assertNotNull(result);
        assertEquals(mockedEntity.getId().toString(), result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(25, result.getAge());
        assertEquals("123 Main St", result.getAddress());
        assertEquals("john.doe@example.com", result.getEmail());

        // Verify that the repository's save method was called with the correct argument
        ArgumentCaptor<CustomerEntity> entityCaptor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerRepository).save(entityCaptor.capture());
        assertEquals("John", entityCaptor.getValue().getFirstName());
        assertEquals("Doe", entityCaptor.getValue().getLastName());
    }

    // Add more test cases for other scenarios, such as validation errors, repository failures, etc.
}
