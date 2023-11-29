package ad.lotfiz.assignment.customerhub.controller;

import ad.lotfiz.assignment.customerhub.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.customerhub.api.v1.CustomerCrudApi;
import nl.customerhub.api.v1.model.CustomerListResponse;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class CustomerController implements CustomerCrudApi {

    private final CustomerService customerService;

    @Override
    public ResponseEntity<CustomerResponse> createNewCustomer(CustomerRequest customerRequest) {
        log.info("Request to create customer {}", customerRequest);
        CustomerResponse response = customerService.createNewCustomer(customerRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Override
    public ResponseEntity<Void> deleteCustomer(String customerId) {
        log.info("Request to delete a customer {}", customerId);
        customerService.delete(customerId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CustomerResponse> getCustomer(String customerId) {
        log.info("Request to get details of a customer {}", customerId);
        CustomerResponse customerResponse = customerService.fetchCustomer(customerId);
        return ResponseEntity.ok(customerResponse);
    }

    @Override
    public ResponseEntity<CustomerListResponse> listCustomers(Integer page, Integer size) {
        Pageable paging = PageRequest.of(page, size);
        log.info("Request to get list of customers {}", paging);
        return ResponseEntity.ok(customerService.list(paging));
    }

    @Override
    public ResponseEntity<CustomerResponse> updateCustomer(String customerId, CustomerRequest customerRequest) {
        log.info("updating the customer id {} with {}", customerId, customerRequest);
        CustomerResponse response = customerService.update(customerId, customerRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CustomerListResponse> findCustomer(String firstName, String lastName) {
        log.info("find a list of customer firstName: {} lastName {}", firstName, lastName);
        Pageable paging = customerService.findByName(firstName, lastName);
        return ResponseEntity.ok(customerService.list(paging));
    }
}
