package ad.lotfiz.assignment.customerhub.repository;

import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository  extends JpaRepository<CustomerEntity, String> {
}
