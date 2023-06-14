package rs.edu.raf.si.bank2.client.repositories.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import rs.edu.raf.si.bank2.client.models.mongodb.TekuciRacun;

import java.util.List;
import java.util.Optional;

public interface TekuciRacunRepository extends MongoRepository<TekuciRacun, String> {

    List<TekuciRacun> findTekuciRacunByOwnerId(String ownerId);

    Optional<TekuciRacun> findTekuciRacunByRegistrationNumber(String registrationNumber);
}
