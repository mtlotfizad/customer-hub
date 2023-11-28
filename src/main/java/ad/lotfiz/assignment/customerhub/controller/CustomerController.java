package ad.lotfiz.assignment.customerhub.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.customerhub.api.v1.CustomerCrudApi;
import nl.customerhub.api.v1.model.CustomerListResponse;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class CustomerController implements CustomerCrudApi {
    @Override
    public ResponseEntity<CustomerResponse> createNewCustomer(CustomerRequest customerRequest) {
        return CustomerCrudApi.super.createNewCustomer(customerRequest);
    }

    @Override
    public ResponseEntity<Void> deleteCustomer(Long customerId) {
        return CustomerCrudApi.super.deleteCustomer(customerId);
    }

    @Override
    public ResponseEntity<CustomerResponse> getCustomer(Long customerId) {
        return CustomerCrudApi.super.getCustomer(customerId);
    }

    @Override
    public ResponseEntity<CustomerListResponse> listCustomers(Integer page, Integer size) {
        return CustomerCrudApi.super.listCustomers(page, size);
    }

    @Override
    public ResponseEntity<CustomerResponse> updateCustomer(Long customerId, CustomerRequest customerRequest) {
        return CustomerCrudApi.super.updateCustomer(customerId, customerRequest);
    }
}
