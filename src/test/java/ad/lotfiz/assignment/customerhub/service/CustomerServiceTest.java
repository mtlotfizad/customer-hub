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

import static ad.lotfiz.assignment.customerhub.RandomGenerator.randomCustomerEntity;
import static ad.lotfiz.assignment.customerhub.RandomGenerator.randomCustomerRequest;
import static ad.lotfiz.assignment.customerhub.RandomGenerator.randomCustomerResponse;
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
        CustomerRequest customerRequest = randomCustomerRequest();
        CustomerEntity mockedEntity = randomCustomerEntity(customerRequest);
        CustomerResponse customerResponse = randomCustomerResponse(mockedEntity);
        when(customerMapper.mapFromCustomerRequest(customerRequest)).thenReturn(mockedEntity);
        when(customerMapper.mapFromCustomerEntity(mockedEntity)).thenReturn(customerResponse);
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(mockedEntity);

        // When
        CustomerResponse result = customerService.createNewCustomer(customerRequest);

        // Then
        assertNotNull(result);
        assertEquals(mockedEntity.getId().toString(), result.getId());
        assertEquals(customerRequest.getFirstName(), result.getFirstName());
        assertEquals(customerRequest.getLastName(), result.getLastName());
        assertEquals(customerRequest.getAge(), result.getAge());
        assertEquals(customerRequest.getAddress(), result.getAddress());
        assertEquals(customerRequest.getEmail(), result.getEmail());

        // Verify that the repository's save method was called with the correct argument
        ArgumentCaptor<CustomerEntity> entityCaptor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerRepository).save(entityCaptor.capture());
        assertEquals(customerRequest.getFirstName(), entityCaptor.getValue().getFirstName());
        assertEquals(customerRequest.getLastName(), entityCaptor.getValue().getLastName());
    }



    @Test
    void testDeleteCustomer() {
        // Given
        CustomerEntity mockedEntity = randomCustomerEntity();
        UUID customerId = mockedEntity.getId();
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

    @Test
    void testFetchCustomer() {
        // Given
        CustomerEntity mockedEntity = randomCustomerEntity();
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockedEntity));
        CustomerResponse customerResponse = randomCustomerResponse(mockedEntity);
        when(customerMapper.mapFromCustomerEntity(mockedEntity)).thenReturn(customerResponse);

        // When
        String uuid = mockedEntity.getId().toString();
        CustomerResponse result = customerService.fetchCustomer(uuid);

        // Then
        assertNotNull(result);
        assertEquals(uuid, result.getId());
        assertEquals(mockedEntity.getFirstName(), result.getFirstName());
        assertEquals(mockedEntity.getLastName(), result.getLastName());
        assertEquals(mockedEntity.getAge(), result.getAge());
        assertEquals(mockedEntity.getAddress(), result.getAddress());
        assertEquals(mockedEntity.getEmail(), result.getEmail());

        // Verify that the repository's findById method was called with the correct argument
        verify(customerRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void testFetchCustomer_customer_not_found() {
        // Given
        String customerId = UUID.randomUUID().toString();
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When and Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.fetchCustomer(customerId));

        // Verify that the repository's findById method was called with the correct argument
        verify(customerRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void testFetchOrThrow_invalid_uuid() {
        // Given
        String invalidUuid = "not_a_valid_uuid";

        // When and Then
        IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class,
                () -> customerService.delete(invalidUuid));

        // Verify the exception message
        assertEquals("Invalid UUID string: not_a_valid_uuid", thrownException.getMessage());

        // Verify that the repository's findById method is not called in case of exception
        verify(customerRepository, never()).findById(any(UUID.class));
    }




}
