package ad.lotfiz.assignment.customerhub.service;

import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import ad.lotfiz.assignment.customerhub.repository.CustomerRepository;
import ad.lotfiz.assignment.customerhub.service.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;

    public CustomerResponse createNewCustomer(CustomerRequest customerRequest) {
        CustomerEntity customerEntity = customerMapper.mapFromCustomerRequest(customerRequest);
        CustomerEntity saved = customerRepository.save(customerEntity);

        return customerMapper.mapFromCustomerEntity(saved);
    }
}
