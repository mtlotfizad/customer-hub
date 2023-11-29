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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;
import java.util.UUID;

import static ad.lotfiz.assignment.customerhub.RandomGenerator.randomCustomerEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CustomerServiceIT {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;

    @Test
    void testCreateNewCustomer_unique_constraint_fails() {
        // Given
        CustomerRequest customerRequest = new CustomerRequest("John", "Doe").age(25).address("123 Main St").email("john.doe@example.com");
        UUID id = UUID.randomUUID();
        CustomerEntity testEntity = randomCustomerEntity();
        customerRepository.save(testEntity);

        // When
        // Then
        assertThrows(DataIntegrityViolationException.class, () -> customerService.createNewCustomer(customerRequest));

    }

}
