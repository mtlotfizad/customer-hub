package ad.lotfiz.assignment.customerhub.service;

import ad.lotfiz.assignment.customerhub.exception.CustomerNotFoundException;
import ad.lotfiz.assignment.customerhub.exception.FieldNotFoundException;
import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import ad.lotfiz.assignment.customerhub.repository.CustomerRepository;
import ad.lotfiz.assignment.customerhub.service.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.customerhub.api.v1.model.CustomerListResponse;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import nl.customerhub.api.v1.model.CustomerUpdateRequest;
import org.apache.logging.log4j.util.Strings;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;

    public CustomerResponse createNewCustomer(CustomerRequest customerRequest) {
        validateMandatoryFields(customerRequest);
        CustomerEntity customerEntity = customerMapper.mapFromCustomerRequest(customerRequest);
        try {
            customerEntity.setCreated(OffsetDateTime.now());
            customerEntity.setUpdated(OffsetDateTime.now());
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
        log.debug("Request to list all Student of page {}", paging);

        Page<CustomerEntity> all = customerRepository.findAll(paging);
        CustomerListResponse response = new CustomerListResponse();
        response.setContent(all.getContent().stream().map(customerMapper::mapFromCustomerEntity).collect(Collectors.toList()));
        response.setPage(all.getNumber());
        response.setSize(all.getSize());

        return response;
    }

    public CustomerListResponse findByName(String firstName, String lastName, Pageable paging) {
        firstName = "%" + Optional.ofNullable(firstName).orElse("") + "%";
        lastName = "%" + Optional.ofNullable(lastName).orElse("") + "%";
        Page<CustomerEntity> customerEntities = customerRepository
                .findByFirstNameLikeAndLastNameLike(firstName, lastName, paging);
        List<CustomerResponse> customerResponses = customerEntities
                .stream()
                .map(customerMapper::mapFromCustomerEntity)
                .collect(Collectors.toList());

        return new CustomerListResponse(
                customerEntities.getNumber(),
                customerEntities.getSize(),
                customerResponses
        );
    }

    public CustomerResponse update(String customerId, CustomerUpdateRequest updateRequest) {
        validateMandatoryFields(updateRequest);
        CustomerEntity existingCustomer = fetchOrThrow(customerId);
        updateCustomerEntity(existingCustomer, updateRequest);
        existingCustomer.setUpdated(OffsetDateTime.now());
        CustomerEntity updatedCustomer = customerRepository.save(existingCustomer);
        return customerMapper.mapFromCustomerEntity(updatedCustomer);
    }

    private void updateCustomerEntity(CustomerEntity customerEntity, CustomerUpdateRequest updateRequest) {
        customerEntity.setAddress(updateRequest.getAddress());
        customerEntity.setEmail(updateRequest.getEmail());
    }

    private CustomerEntity fetchOrThrow(String id) {
        UUID uuid = UUID.fromString(id);
        return customerRepository.findById(uuid).orElseThrow(() -> new CustomerNotFoundException(String.format("Customer %s not found", id)));
    }

    private static void validateMandatoryFields(CustomerRequest customerRequest) {
        if (Strings.isEmpty(customerRequest.getAddress()) && Strings.isEmpty(customerRequest.getEmail())) {
            throw new FieldNotFoundException("Either Address or email should be provided");
        }
    }

    private static void validateMandatoryFields(CustomerUpdateRequest customerRequest) {
        if (Strings.isEmpty(customerRequest.getAddress()) && Strings.isEmpty(customerRequest.getEmail())) {
            throw new FieldNotFoundException("Either Address or email should be provided");
        }
    }

}
