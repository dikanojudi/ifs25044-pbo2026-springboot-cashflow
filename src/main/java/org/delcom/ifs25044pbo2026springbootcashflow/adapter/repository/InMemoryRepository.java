package org.delcom.ifs25044pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25044pbo2026springbootcashflow.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Basis generik repository in-memory. Menghilangkan duplikasi kode
 * penyimpanan list dan pencarian untuk berbagai entitas.
 *
 * @param <T>  tipe entitas domain
 * @param <ID> tipe identifier entitas
 */
public abstract class InMemoryRepository<T, ID> implements IRepository<T, ID> {
    /** Penyimpanan data di memori. */
    protected final List<T> data = new ArrayList<>();

    /** Fungsi pengambil ID dari entitas. */
    private final Function<T, ID> idExtractor;

    protected InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public List<T> findAll() {
        // Salinan defensif agar data internal tidak bisa dimutasi dari luar.
        return new ArrayList<>(data);
    }

    @Override
    public Optional<T> findById(ID id) {
        return data.stream()
                .filter(item -> idExtractor.apply(item).equals(id))
                .findFirst();
    }

    @Override
    public T save(T entity) {
        data.add(entity);
        return entity;
    }

    @Override
    public boolean deleteById(ID id) {
        return data.removeIf(item -> idExtractor.apply(item).equals(id));
    }

    @Override
    public void update(T entity) {
        // Objek mutable disimpan by-reference, jadi tidak ada yang perlu disalin.
    }

    @Override
    public List<T> findBy(Predicate<T> predicate) {
        return data.stream()
                .filter(predicate)
                .toList();
    }
}
