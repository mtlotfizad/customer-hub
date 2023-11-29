package ad.lotfiz.assignment.customerhub.repository;

import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerRepository  extends JpaRepository<CustomerEntity, UUID> {
    Page<CustomerEntity> findByFirstNameAndLastName(String firstName, String lastName, Pageable paging);

}
