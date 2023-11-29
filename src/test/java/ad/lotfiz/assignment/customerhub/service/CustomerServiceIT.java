package ad.lotfiz.assignment.customerhub.service;

import ad.lotfiz.assignment.customerhub.exception.CustomerNotFoundException;
import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import ad.lotfiz.assignment.customerhub.repository.CustomerRepository;
import ad.lotfiz.assignment.customerhub.service.mapper.CustomerMapper;
import nl.customerhub.api.v1.model.CustomerListResponse;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static ad.lotfiz.assignment.customerhub.RandomGenerator.mapRequestToEntity;
import static ad.lotfiz.assignment.customerhub.RandomGenerator.randomCustomerEntity;
import static ad.lotfiz.assignment.customerhub.RandomGenerator.randomCustomerRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class CustomerServiceIT {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;


    @BeforeEach
    public void cleanUpDatabase(){
        customerRepository.deleteAll();
    }
    @Test
    void testCreateNewCustomer_happy_flow() {
        // Given
        CustomerRequest customerRequest = randomCustomerRequest();

        // When
        CustomerResponse result = customerService.createNewCustomer(customerRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(customerRequest.getFirstName(), result.getFirstName());
        assertEquals(customerRequest.getLastName(), result.getLastName());
        assertEquals(customerRequest.getAge(), result.getAge());
        assertEquals(customerRequest.getAddress(), result.getAddress());
        assertEquals(customerRequest.getEmail(), result.getEmail());
    }

    @Test
    void testCreateNewCustomer_unique_constraint_fails() {
        // Given
        CustomerRequest customerRequest = randomCustomerRequest();
        CustomerEntity testEntity = mapRequestToEntity(customerRequest);
        customerRepository.save(testEntity);

        // When
        // Then
        assertThrows(DataIntegrityViolationException.class, () -> customerService.createNewCustomer(customerRequest));
    }

    @Test
    void testDeleteCustomer() {
        // Given
        CustomerEntity testEntity = randomCustomerEntity();
        customerRepository.save(testEntity);

        // When
        customerService.delete(testEntity.getId().toString());

        // Then
        assertFalse(customerRepository.existsById(testEntity.getId()));
    }

    @Test
    void testDeleteCustomer_customer_not_found() {
        // Given
        String nonExistingCustomerId = UUID.randomUUID().toString();

        // When and Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.delete(nonExistingCustomerId));
    }

    @Test
    void testFetchCustomer() {
        // Given
        CustomerEntity testEntity = randomCustomerEntity();
        customerRepository.save(testEntity);

        // When
        CustomerResponse result = customerService.fetchCustomer(testEntity.getId().toString());

        // Then
        assertNotNull(result);
        assertEquals(testEntity.getId().toString(), result.getId());
        assertEquals(testEntity.getFirstName(), result.getFirstName());
        assertEquals(testEntity.getLastName(), result.getLastName());
        assertEquals(testEntity.getAge(), result.getAge());
        assertEquals(testEntity.getAddress(), result.getAddress());
        assertEquals(testEntity.getEmail(), result.getEmail());
    }

    @Test
    void testFetchCustomer_customer_not_found() {
        // Given
        String nonExistingCustomerId = UUID.randomUUID().toString();

        // When and Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.fetchCustomer(nonExistingCustomerId));
    }


    @Test
    void testListCustomers() {
        // Given
        List<CustomerEntity> customerEntities = Arrays.asList(
                randomCustomerEntity(),
                randomCustomerEntity(),
                randomCustomerEntity(),
                randomCustomerEntity(),
                randomCustomerEntity(),
                randomCustomerEntity(),
                randomCustomerEntity()
        );
        customerRepository.saveAll(customerEntities);

        // When
        Pageable paging = PageRequest.of(1, 4); // page 2
        CustomerListResponse result = customerService.list(paging);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getPage());
        assertEquals(4, result.getSize());
        assertEquals(3, result.getContent().size());

        // Verify that the content matches the expected values
        for (int i = 4; i < customerEntities.size(); i++) {
            CustomerEntity entity = customerEntities.get(i);
            CustomerResponse response = result.getContent().get(i - 4);

            assertEquals(entity.getId().toString(), response.getId());
            assertEquals(entity.getFirstName(), response.getFirstName());
            assertEquals(entity.getLastName(), response.getLastName());
            assertEquals(entity.getAge(), response.getAge());
            assertEquals(entity.getAddress(), response.getAddress());
            assertEquals(entity.getEmail(), response.getEmail());
        }
    }

    @Test
    void testFindCustomersByFirstName_LastName() {
        // Given
        List<CustomerEntity> customerEntities = Arrays.asList(
                randomCustomerEntity("John", "Doe"),
                randomCustomerEntity("Jane", "Doe"),
                randomCustomerEntity("Alice", "Smith")
        );
        customerRepository.saveAll(customerEntities);

        // When
        String firstNameToSearch = "John";
        String lastNameToSearch = "Doe";
        Pageable paging = PageRequest.of(0, 10);
        CustomerListResponse result = customerService.findByName(firstNameToSearch, lastNameToSearch, paging);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());

        // Verify that the content matches the expected values
        List<CustomerResponse> customerResponses = result.getContent();
        assertEquals(1, customerResponses.size());
        CustomerResponse response = customerResponses.get(0);
        assertEquals(firstNameToSearch, response.getFirstName());
        assertEquals(lastNameToSearch, response.getLastName());
    }


    @Test
    void testFindCustomersByLastName() {
        // Given
        List<CustomerEntity> customerEntities = Arrays.asList(
                randomCustomerEntity("John", "Doe"),
                randomCustomerEntity("Jane", "Doe"),
                randomCustomerEntity("Alice", "Smith")
        );
        customerRepository.saveAll(customerEntities);

        // When
        String lastNameToSearch = "Doe";
        Pageable paging = PageRequest.of(0, 10);
        CustomerListResponse result = customerService.findByName(null, lastNameToSearch, paging);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());

        // Verify that the content matches the expected values
        List<CustomerResponse> customerResponses = result.getContent();
        assertEquals(2, customerResponses.size());
        CustomerResponse response = customerResponses.get(0);
        assertEquals(lastNameToSearch, response.getLastName());
    }


}
