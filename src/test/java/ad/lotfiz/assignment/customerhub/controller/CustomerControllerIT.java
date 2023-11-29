package ad.lotfiz.assignment.customerhub.controller;

import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import ad.lotfiz.assignment.customerhub.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.customerhub.api.v1.model.CustomerListResponse;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static ad.lotfiz.assignment.customerhub.RandomGenerator.randomCustomerEntity;
import static ad.lotfiz.assignment.customerhub.RandomGenerator.randomCustomerRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
public class CustomerControllerIT {
    private static final String CUSTOMERS_PATH = "/customers";
    private static final String ONE_CUSTOMER_PATH = "/customers/%S";

    @Autowired
    protected TestRestTemplate restTemplate;
    @Autowired
    protected ObjectMapper jsonObjectMapper;
    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    public void cleanUpDatabase() {
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("should create new Customer")
    void shouldCreateNewCustomer() {
        //given
        var request = randomCustomerRequest();
        //when
        ResponseEntity<CustomerResponse> response = restTemplate.postForEntity(CUSTOMERS_PATH, request, CustomerResponse.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String[] splittedLocation = Objects.requireNonNull(response.getHeaders().get(HttpHeaders.LOCATION)).get(0).split("/");
        String id = splittedLocation[splittedLocation.length - 1];
        CustomerEntity entity = customerRepository.findById(UUID.fromString(id)).orElseThrow();

        assertThat(entity.getFirstName()).isEqualTo(request.getFirstName());
        assertThat(entity.getLastName()).isEqualTo(request.getLastName());
        assertThat(entity.getAddress()).isEqualTo(request.getAddress());
        assertThat(entity.getAge()).isEqualTo(request.getAge());
        assertThat(entity.getEmail()).isEqualTo(request.getEmail());

        CustomerResponse body = response.getBody();
        assertNotNull(body);
        assertThat(entity.getFirstName()).isEqualTo(body.getFirstName());
        assertThat(entity.getLastName()).isEqualTo(body.getLastName());
        assertThat(entity.getAddress()).isEqualTo(body.getAddress());
        assertThat(entity.getAge()).isEqualTo(body.getAge());
        assertThat(entity.getEmail()).isEqualTo(body.getEmail());
    }

    @Test
    @DisplayName("should delete existing customer")
    void shouldDeleteExistingCustomer() {
        //given
        var entity = customerRepository.save(randomCustomerEntity());
        //when
        ResponseEntity<Void> response =
                deleteEntity(String.format(ONE_CUSTOMER_PATH, entity.getId().toString()), Void.class);
        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(customerRepository.findById(entity.getId())).isEmpty();
    }

    @Test
    @DisplayName("should return error when try to delete non existing customer")
    void shouldReturnError_when_customer_not_found() {
        //given
        //when
        ResponseEntity<Void> response =
                deleteEntity(String.format(ONE_CUSTOMER_PATH, UUID.randomUUID()), Void.class);
        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("should fetch existing customer's details")
    void shouldFetchCustomer() {
        //given
        var entity = customerRepository.save(randomCustomerEntity());
        //when
        ResponseEntity<CustomerResponse> response =
                restTemplate.getForEntity(String.format(ONE_CUSTOMER_PATH, entity.getId().toString()), CustomerResponse.class);
        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CustomerResponse body = response.getBody();
        assertNotNull(body);
        assertThat(entity.getFirstName()).isEqualTo(body.getFirstName());
        assertThat(entity.getLastName()).isEqualTo(body.getLastName());
        assertThat(entity.getAddress()).isEqualTo(body.getAddress());
        assertThat(entity.getAge()).isEqualTo(body.getAge());
        assertThat(entity.getEmail()).isEqualTo(body.getEmail());
    }

    @Test
    @DisplayName("should return list of customers")
    void shouldFetchCustomersList() {
        //given
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

        //when
        ResponseEntity<CustomerListResponse> response =
                restTemplate.getForEntity(CUSTOMERS_PATH + "?page=1&size=4", CustomerListResponse.class);
        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CustomerListResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.getPage());
        assertEquals(4, body.getSize());
        assertEquals(3, body.getContent().size());

        for (int i = 4; i < customerEntities.size(); i++) {
            CustomerEntity entity = customerEntities.get(i);
            CustomerResponse customerResponse = body.getContent().get(i - 4);

            assertEquals(entity.getId().toString(), customerResponse.getId());
            assertEquals(entity.getFirstName(), customerResponse.getFirstName());
            assertEquals(entity.getLastName(), customerResponse.getLastName());
            assertEquals(entity.getAge(), customerResponse.getAge());
            assertEquals(entity.getAddress(), customerResponse.getAddress());
            assertEquals(entity.getEmail(), customerResponse.getEmail());
        }
    }

    @Test
    void testUpdateCustomer() {
        // Given
        CustomerEntity existingCustomer = randomCustomerEntity();
        CustomerEntity entity = customerRepository.save(existingCustomer);
        CustomerRequest updatedRequest = randomCustomerRequest();

        // When
        String url = String.format(ONE_CUSTOMER_PATH, entity.getId().toString());
        ResponseEntity<CustomerResponse> response = putEntity(url, updatedRequest, CustomerResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CustomerResponse body = response.getBody();
        assertNotNull(body);

        Assertions.assertThat(body)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(OffsetDateTime.class)
                .ignoringFields("id")
                .isEqualTo(updatedRequest);

        OffsetDateTime currentTime = OffsetDateTime.now();
        Duration acceptableTimeDifference = Duration.ofSeconds(5);
        assertTrue("Updated time should be near the current time",
                Math.abs(Duration.between(body.getUpdated(), currentTime).getSeconds()) <= acceptableTimeDifference.getSeconds());
    }

    @Test
    void testFindCustomer() {
        List<CustomerEntity> customerEntities = Arrays.asList(
                randomCustomerEntity("John", "Doe"),
                randomCustomerEntity("Jane", "Doe"),
                randomCustomerEntity("Alice", "Smith")
        );
        customerRepository.saveAll(customerEntities);

        // When
        String lastNameToSearch = "Doe";
        String url = "/customers/find?lastName=" + lastNameToSearch + "&page=0&size=10";
        ResponseEntity<CustomerListResponse> getResponse = restTemplate.getForEntity(url, CustomerListResponse.class);

        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CustomerListResponse body = getResponse.getBody();
        assertNotNull(body);
        assertEquals(0, body.getPage());
        assertEquals(10, body.getSize());
        assertNotNull(body.getContent());
        assertThat(body.getContent().size()).isEqualTo(2);
        // Verify that the content matches the expected values

        Assertions.assertThat(body.getContent().get(0))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(OffsetDateTime.class)
                .ignoringFields("id")
                .isEqualTo(customerEntities.get(0));

        Assertions.assertThat(body.getContent().get(1))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(OffsetDateTime.class)
                .ignoringFields("id")
                .isEqualTo(customerEntities.get(1));

    }


    private <T, U> ResponseEntity<U> putEntity(String url, T body, Class<U> classResponse) {
        HttpEntity<T> entity = new HttpEntity<>(body);
        return restTemplate.exchange(url, HttpMethod.PUT, entity, classResponse);
    }

    private <U> ResponseEntity<U> deleteEntity(String url, Class<U> classResponse) {

        return restTemplate.exchange(url, HttpMethod.DELETE, null, classResponse);
    }


}
