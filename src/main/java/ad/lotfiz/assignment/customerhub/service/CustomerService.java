package ad.lotfiz.assignment.customerhub.service;

import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import ad.lotfiz.assignment.customerhub.repository.CustomerRepository;
import ad.lotfiz.assignment.customerhub.service.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

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
}
