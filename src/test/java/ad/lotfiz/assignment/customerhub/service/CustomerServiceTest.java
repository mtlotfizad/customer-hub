package ad.lotfiz.assignment.customerhub.service;

import ad.lotfiz.assignment.customerhub.RandomGenerator;
import ad.lotfiz.assignment.customerhub.exception.CustomerNotFoundException;
import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import ad.lotfiz.assignment.customerhub.repository.CustomerRepository;
import ad.lotfiz.assignment.customerhub.service.mapper.CustomerMapper;
import nl.customerhub.api.v1.model.CustomerListResponse;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static ad.lotfiz.assignment.customerhub.RandomGenerator.mapEntityToResponse;
import static ad.lotfiz.assignment.customerhub.RandomGenerator.mapRequestToEntity;
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
        CustomerEntity mockedEntity = RandomGenerator.mapRequestToEntity(customerRequest);
        CustomerResponse customerResponse = RandomGenerator.mapEntityToResponse(mockedEntity);
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
        CustomerResponse customerResponse = RandomGenerator.mapEntityToResponse(mockedEntity);
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


    @Test
    void testListCustomers() {
        // Given
        int page = 0;
        int size = 10;
        Pageable paging = PageRequest.of(page, size);

        // Mocking the behavior of CustomerRepository
        List<CustomerEntity> mockedEntities = Arrays.asList(
                randomCustomerEntity(),
                randomCustomerEntity(),
                randomCustomerEntity()
        );
        Page<CustomerEntity> mockedPage = new PageImpl<>(mockedEntities, paging, mockedEntities.size());
        when(customerRepository.findAll(paging)).thenReturn(mockedPage);

        // Mocking the behavior of CustomerMapper
        List<CustomerResponse> mockedResponses = mockedEntities
                .stream()
                .map(RandomGenerator::mapEntityToResponse)
                .collect(Collectors.toList());
        when(customerMapper.mapFromCustomerEntity(mockedEntities.get(0))).thenReturn(mockedResponses.get(0));
        when(customerMapper.mapFromCustomerEntity(mockedEntities.get(1))).thenReturn(mockedResponses.get(1));
        when(customerMapper.mapFromCustomerEntity(mockedEntities.get(2))).thenReturn(mockedResponses.get(2));

        // When
        CustomerListResponse result = customerService.list(paging);

        // Then
        assertNotNull(result);
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(mockedResponses, result.getContent());

        // Verify that the repository's findAll method was called with the correct argument
        verify(customerRepository, times(1)).findAll(paging);

        // Verify that the mapper's mapFromCustomerEntity method was called for each entity
        verify(customerMapper, times(mockedEntities.size())).mapFromCustomerEntity(any());
    }

    @Test
    void testFindCustomersByName() {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        int page = 0;
        int size = 10;
        Pageable paging = PageRequest.of(page, size);

        // Mocking the behavior of CustomerRepository
        List<CustomerEntity> mockedEntities = Arrays.asList(
                randomCustomerEntity(firstName, lastName),
                randomCustomerEntity(firstName + "1", lastName + "1"),
                randomCustomerEntity(firstName + "2", lastName + "2")
        );
        Page<CustomerEntity> mockedPage = new PageImpl<>(mockedEntities, paging, mockedEntities.size());
        when(customerRepository.findByFirstNameAndLastName(firstName, lastName, paging)).thenReturn(mockedPage);

        // Mocking the behavior of CustomerMapper
        List<CustomerResponse> mockedResponses = mockedEntities
                .stream()
                .map(RandomGenerator::mapEntityToResponse)
                .collect(Collectors.toList());
        when(customerMapper.mapFromCustomerEntity(mockedEntities.get(0))).thenReturn(mockedResponses.get(0));
        when(customerMapper.mapFromCustomerEntity(mockedEntities.get(1))).thenReturn(mockedResponses.get(1));
        when(customerMapper.mapFromCustomerEntity(mockedEntities.get(2))).thenReturn(mockedResponses.get(2));

        // When
        CustomerListResponse result = customerService.findByName(firstName, lastName, paging);

        // Then
        assertNotNull(result);
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(mockedResponses, result.getContent());

        // Verify that the repository's findByFirstNameAndLastName method was called with the correct arguments
        verify(customerRepository, times(1)).findByFirstNameAndLastName(firstName, lastName, paging);

        // Verify that the mapper's mapFromCustomerEntity method was called for each entity
        verify(customerMapper, times(mockedEntities.size())).mapFromCustomerEntity(any());
    }


    @Test
    void testUpdateCustomer_happy_flow() {
        // Given
        CustomerRequest customerRequest = randomCustomerRequest();
        CustomerEntity existingCustomer = randomCustomerEntity();
        UUID customerId = existingCustomer.getId();
        CustomerEntity updatedCustomer = mapRequestToEntity(customerRequest);
        CustomerResponse expectedResponse = mapEntityToResponse(updatedCustomer);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(updatedCustomer);
        when(customerMapper.mapFromCustomerEntity(updatedCustomer)).thenReturn(expectedResponse);

        // When
        CustomerResponse result = customerService.update(customerId.toString(), customerRequest);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);

        // Verify that the repository's save method was called with the correct argument
        ArgumentCaptor<CustomerEntity> entityCaptor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerRepository).save(entityCaptor.capture());
        assertEquals(customerRequest.getFirstName(), entityCaptor.getValue().getFirstName());
        assertEquals(customerRequest.getLastName(), entityCaptor.getValue().getLastName());

        // Verify that the fetchOrThrow method was called with the correct argument
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void testUpdateCustomer_duplicate_data_exception() {
        // Given
        UUID customerId = UUID.randomUUID();
        CustomerRequest customerRequest = randomCustomerRequest();
        CustomerEntity existingCustomer = randomCustomerEntity();

        when(customerRepository.findById(customerId)).thenReturn(Optional.ofNullable(existingCustomer));
        when(customerRepository.save(any(CustomerEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // When and Then
        DataIntegrityViolationException thrownException = assertThrows(DataIntegrityViolationException.class,
                () -> customerService.update(customerId.toString(), customerRequest));

        // Verify the exception message
        assertEquals("Duplicate entry", thrownException.getMessage());

        // Verify that the fetchOrThrow method was called with the correct argument
        verify(customerRepository, times(1)).findById(customerId);

        // Verify that the repository's save method was called with the correct argument
        ArgumentCaptor<CustomerEntity> entityCaptor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerRepository).save(entityCaptor.capture());
        assertEquals(customerRequest.getFirstName(), entityCaptor.getValue().getFirstName());
        assertEquals(customerRequest.getLastName(), entityCaptor.getValue().getLastName());
    }
}
