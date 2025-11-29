package ru.mtuci.rbpo2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mtuci.rbpo2025.model.Parcel;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long> {
}
