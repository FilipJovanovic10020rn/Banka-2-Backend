package rs.edu.raf.si.bank2.client.repositories.mongodb;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import rs.edu.raf.si.bank2.client.models.mongodb.TekuciRacun;

@Repository
public interface TekuciRacunRepository extends MongoRepository<TekuciRacun, String> {

    List<TekuciRacun> findTekuciRacunByOwnerId(String ownerId);

    Optional<TekuciRacun> findTekuciRacunByRegistrationNumber(String registrationNumber);
}
