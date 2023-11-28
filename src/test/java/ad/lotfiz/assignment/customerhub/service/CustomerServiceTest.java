package ad.lotfiz.assignment.customerhub.service;

import ad.lotfiz.assignment.customerhub.exception.CustomerNotFoundException;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    void testCreateNewCustomer_happy_flow() {
        // Given
        CustomerRequest customerRequest = new CustomerRequest("John", "Doe").age(25).address("123 Main St").email("john.doe@example.com");
        CustomerEntity mockedEntity = getMockedEntity();
        CustomerResponse customerResponse = getMockedResponse(mockedEntity);
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


    @Test
    void testDeleteCustomer() {
        // Given
        UUID customerId = UUID.randomUUID();

        // Mocking the behavior of CustomerRepository
        CustomerEntity mockedEntity = getMockedEntity();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockedEntity));

        // When
        customerService.delete(customerId.toString());

        // Then
        verify(customerRepository, times(1)).delete(mockedEntity);
    }

    @Test
    void testDeleteCustomer_customer_not_found() {
        // Given
        UUID customerId = UUID.randomUUID();

        // Mocking the behavior of CustomerRepository
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When and Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.delete(customerId.toString()));

        // Verify that the repository's delete method is not called in case of exception
        verify(customerRepository, never()).delete(any(CustomerEntity.class));
    }

    private static CustomerEntity getMockedEntity() {
        UUID id = UUID.randomUUID();
        return new CustomerEntity(id, "John", "Doe", 25, "123 Main St", "john.doe@example.com");
    }

    private static CustomerResponse getMockedResponse(CustomerEntity mockedEntity) {
        return new CustomerResponse(mockedEntity.getId().toString(), OffsetDateTime.now(), "John", "Doe").age(25).address("123 Main St").email("john.doe@example.com");
    }


}
