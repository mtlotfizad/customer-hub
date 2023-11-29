package ad.lotfiz.assignment.customerhub.service;

import ad.lotfiz.assignment.customerhub.exception.CustomerNotFoundException;
import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import ad.lotfiz.assignment.customerhub.repository.CustomerRepository;
import ad.lotfiz.assignment.customerhub.service.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.customerhub.api.v1.model.CustomerListResponse;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;

    public CustomerResponse createNewCustomer(CustomerRequest customerRequest) {
        CustomerEntity customerEntity = customerMapper.mapFromCustomerRequest(customerRequest);
        try {
            CustomerEntity saved = customerRepository.save(customerEntity);

            return customerMapper.mapFromCustomerEntity(saved);
        } catch (DataIntegrityViolationException e) {
            log.error("Create customer failed: {}", customerRequest, e);
            throw e;
        }
    }

    public void delete(String customerId) {
        customerRepository.delete(fetchOrThrow(customerId));
    }

    public CustomerResponse fetchCustomer(String uuid) {
        return customerMapper.mapFromCustomerEntity(fetchOrThrow(uuid));
    }


    public CustomerListResponse list(Pageable paging) {
        return null;
    }

    public Pageable findByName(String firstName, String lastName) {
        return null;
    }

    public CustomerResponse update(String customerId, CustomerRequest customerRequest) {
        return null;
    }

    private CustomerEntity fetchOrThrow(String id) {
        UUID uuid = UUID.fromString(id);
        return customerRepository.findById(uuid).orElseThrow(() -> new CustomerNotFoundException(String.format("Customer %s not found", id)));
    }


}
