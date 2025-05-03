package iset.pfe.mediconnectback.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>{
    
}
