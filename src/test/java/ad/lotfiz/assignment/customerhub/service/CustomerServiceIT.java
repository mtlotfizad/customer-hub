package ad.lotfiz.assignment.customerhub.service;

import ad.lotfiz.assignment.customerhub.exception.CustomerNotFoundException;
import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import ad.lotfiz.assignment.customerhub.repository.CustomerRepository;
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

import java.time.Duration;
import java.time.OffsetDateTime;
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
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest
public class CustomerServiceIT {


    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;


    @BeforeEach
    public void cleanUpDatabase() {
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

    @Test
    void testUpdateCustomer() {
        // Given
        CustomerEntity existingCustomer = randomCustomerEntity();
        customerRepository.save(existingCustomer);

        CustomerRequest updatedRequest = randomCustomerRequest();

        // When
        CustomerResponse result = customerService.update(existingCustomer.getId().toString(), updatedRequest);

        // Then
        assertNotNull(result);
        assertEquals(existingCustomer.getId().toString(), result.getId());
        assertEquals(updatedRequest.getFirstName(), result.getFirstName());
        assertEquals(updatedRequest.getLastName(), result.getLastName());
        assertEquals(updatedRequest.getAge(), result.getAge());
        assertEquals(updatedRequest.getAddress(), result.getAddress());
        assertEquals(updatedRequest.getEmail(), result.getEmail());
        assertNotNull(result.getUpdated());
        OffsetDateTime currentTime = OffsetDateTime.now();
        Duration acceptableTimeDifference = Duration.ofSeconds(5);
        assertTrue("Updated time should be near the current time",
                Math.abs(Duration.between(result.getUpdated(), currentTime).getSeconds()) <= acceptableTimeDifference.getSeconds());
    }

    @Test
    void testUpdateCustomer_duplicate_constraint_fails() {
        // Given
        CustomerEntity existingCustomer1 = randomCustomerEntity("John", "Doe");
        CustomerEntity existingCustomer2 = randomCustomerEntity("Jane", "Doe");
        customerRepository.saveAll(Arrays.asList(existingCustomer1, existingCustomer2));

        CustomerRequest updatedRequest = new CustomerRequest()
                .firstName("Jane") // Attempt to update with a duplicate firstName
                .lastName("Doe")
                .age(30)
                .address("UpdatedAddress")
                .email("updated.email@example.com");

        // When and Then
        assertThrows(DataIntegrityViolationException.class,
                () -> customerService.update(existingCustomer1.getId().toString(), updatedRequest));
    }

    @Test
    void testUpdateCustomer_customer_not_found() {
        // Given
        String nonExistingCustomerId = UUID.randomUUID().toString();
        CustomerRequest updatedRequest = randomCustomerRequest();

        // When and Then
        assertThrows(CustomerNotFoundException.class,
                () -> customerService.update(nonExistingCustomerId, updatedRequest));
    }

}
